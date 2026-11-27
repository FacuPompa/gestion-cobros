package com.facupompa.pellet.domain.service;

import com.facupompa.pellet.domain.dto.*;
import com.facupompa.pellet.domain.exception.*;
import com.facupompa.pellet.domain.model.PaymentMethod;
import com.facupompa.pellet.persistence.entity.*;
import com.facupompa.pellet.persistence.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PaymentCancellationIntegrationTest {
    @Autowired private CustomerRepository customers;
    @Autowired private DebtRepository debts;
    @Autowired private PaymentRepository payments;
    @Autowired private PaymentService paymentService;
    @Autowired private DebtService debtService;
    @Autowired private CustomerService customerService;
    @Autowired private EntityManager entityManager;

    private CustomerEntity customer;
    private DebtEntity debt;
    private PaymentEntity payment;
    private final Instant registeredAt = Instant.parse("2026-11-18T12:00:00Z");

    @BeforeEach
    void setUp() {
        customer = customers.save(new CustomerEntity("Cliente de prueba de anulaciones"));
        debt = debts.save(new DebtEntity(customer, "Deuda de prueba", new BigDecimal("50000.25"),
                LocalDate.of(2026, 12, 18)));
        payment = payments.save(new PaymentEntity(debt, new BigDecimal("10000.00"),
                registeredAt, PaymentMethod.CASH, "operador"));
    }

    @Test
    void cancellationShouldBePersistedWithoutDeletingOriginalPayment() {
        Long paymentId = payment.getId();
        CancelPaymentResponse response = paymentService.cancelPayment(paymentId,
                new CancelPaymentRequest("Registro duplicado"), "admin");
        entityManager.flush();
        entityManager.clear();

        PaymentEntity stored = payments.findById(paymentId).orElseThrow();
        assertTrue(stored.isCancelled());
        assertEquals("operador", stored.getRegisteredBy());
        assertEquals("admin", stored.getCancelledBy());
        assertEquals("admin", response.cancelledBy());
        assertEquals(new BigDecimal("10000.00"), stored.getAmount());
        assertEquals(debt.getId(), stored.getDebt().getId());
        assertEquals(registeredAt, stored.getRegisteredAt());
        assertEquals("Registro duplicado", stored.getCancellationReason());
        assertNotNull(stored.getCancelledAt());
        assertEquals(paymentId, response.paymentId());
        assertEquals(debt.getId(), response.debtId());
        assertEquals(stored.getAmount(), response.amount());
        assertEquals(stored.getCancellationReason(), response.cancellationReason());
        // PostgreSQL conserva microsegundos; Instant.now puede incluir nanosegundos.
        assertTrue(java.time.Duration.between(response.cancelledAt(), stored.getCancelledAt())
                .abs().compareTo(java.time.Duration.ofNanos(1000)) <= 0);
    }

    @Test
    void cancelledPaymentsShouldBeExcludedFromDebtAndCustomerBalances() {
        DebtEntity otherDebt = debts.save(new DebtEntity(customer, "Otra deuda",
                new BigDecimal("20000.00"), LocalDate.of(2026, 12, 18)));
        payments.save(new PaymentEntity(otherDebt, new BigDecimal("5000.00"),
                registeredAt, PaymentMethod.BANK_TRANSFER, "operador"));
        paymentService.cancelPayment(payment.getId(), new CancelPaymentRequest("Duplicado"), "admin");
        entityManager.flush();
        entityManager.clear();

        DebtBalanceResponse debtBalance = debtService.findDebtBalanceById(debt.getId());
        assertMoney("0", debtBalance.totalPaid());
        assertMoney("50000.25", debtBalance.remainingBalance());
        CustomerBalanceResponse balance = customerService.findCustomerBalanceById(customer.getId());
        assertMoney("70000.25", balance.totalOriginalAmount());
        assertMoney("5000.00", balance.totalPaid());
        assertMoney("65000.25", balance.remainingBalance());
    }

    @Test
    void newPaymentShouldUseBalanceRecoveredByCancellation() {
        paymentService.cancelPayment(payment.getId(), new CancelPaymentRequest("Duplicado"), "admin");
        PaymentResponse created = paymentService.registerPayment(new CreatePaymentRequest(
                debt.getId(), new BigDecimal("45000.00"), PaymentMethod.CARD), "operador");
        entityManager.flush();
        entityManager.clear();

        assertNotNull(created.id());
        assertEquals("operador", created.registeredBy());
        assertEquals("operador", payments.findById(created.id()).orElseThrow().getRegisteredBy());
        assertTrue(payments.findById(payment.getId()).orElseThrow().isCancelled());
        DebtBalanceResponse balance = debtService.findDebtBalanceById(debt.getId());
        assertMoney("45000", balance.totalPaid());
        assertMoney("5000.25", balance.remainingBalance());
    }

    @Test
    void historyShouldKeepCancelledAndActivePaymentsInStableOrder() {
        // Igual fecha: el identificador debe desempatar.
        PaymentEntity second = payments.save(new PaymentEntity(debt, new BigDecimal("100.00"),
                registeredAt, PaymentMethod.CARD, "operador"));
        paymentService.cancelPayment(payment.getId(), new CancelPaymentRequest("Duplicado"), "admin");
        entityManager.flush();
        entityManager.clear();

        List<PaymentHistoryResponse> history = paymentService.findPaymentHistoryByDebtId(debt.getId());
        assertEquals(List.of(payment.getId(), second.getId()),
                history.stream().map(PaymentHistoryResponse::id).toList());
        assertEquals("Duplicado", history.getFirst().cancellationReason());
        assertNotNull(history.getFirst().cancelledAt());
        assertMoney("10000", history.getFirst().amount());
        assertEquals("operador", history.getFirst().registeredBy());
        assertEquals("admin", history.getFirst().cancelledBy());
        assertEquals("operador", history.getLast().registeredBy());
        assertNull(history.getLast().cancelledBy());
        assertNull(history.getLast().cancelledAt());
        assertNull(history.getLast().cancellationReason());
    }

    @Test
    void cancellingMissingPaymentShouldFail() {
        // El fixture genera IDs positivos; un ID negativo no puede existir aquí.
        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.cancelPayment(-1L, new CancelPaymentRequest("Error"), "admin"));
    }

    @Test
    void repeatedCancellationShouldPreservePersistedActor() {
        paymentService.cancelPayment(payment.getId(), new CancelPaymentRequest("Primer motivo"), "admin");
        entityManager.flush();
        entityManager.clear();
        Instant firstCancellation = payments.findById(payment.getId()).orElseThrow().getCancelledAt();

        assertThrows(PaymentAlreadyCancelledException.class,
                () -> paymentService.cancelPayment(payment.getId(),
                        new CancelPaymentRequest("Otro motivo"), "otro-admin"));
        PaymentEntity stored = payments.findById(payment.getId()).orElseThrow();
        assertEquals("admin", stored.getCancelledBy());
        assertEquals("operador", stored.getRegisteredBy());
        assertEquals("Primer motivo", stored.getCancellationReason());
        assertEquals(firstCancellation, stored.getCancelledAt());
    }

    @Test
    void legacyPaymentShouldKeepUnknownAuthorWhenCancelled() {
        entityManager.createNativeQuery("UPDATE payments SET registered_by = NULL WHERE id = :id")
                .setParameter("id", payment.getId()).executeUpdate();
        entityManager.clear();
        paymentService.cancelPayment(payment.getId(), new CancelPaymentRequest("Error antiguo"), "admin");
        entityManager.flush();
        entityManager.clear();

        PaymentEntity stored = payments.findById(payment.getId()).orElseThrow();
        assertNull(stored.getRegisteredBy());
        assertEquals("admin", stored.getCancelledBy());
        PaymentHistoryResponse history = paymentService.findPaymentHistoryByDebtId(debt.getId()).getFirst();
        assertNull(history.registeredBy());
        assertEquals("admin", history.cancelledBy());
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "Esperado " + expected + ", obtenido " + actual);
    }
}
