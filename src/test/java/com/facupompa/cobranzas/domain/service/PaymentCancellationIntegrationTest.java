package com.facupompa.cobranzas.domain.service;

import com.facupompa.cobranzas.domain.dto.*;
import com.facupompa.cobranzas.domain.exception.*;
import com.facupompa.cobranzas.domain.model.PaymentMethod;
import com.facupompa.cobranzas.persistence.entity.*;
import com.facupompa.cobranzas.persistence.repository.*;
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
                registeredAt, PaymentMethod.CASH));
    }

    @Test
    void cancellationShouldBePersistedWithoutDeletingOriginalPayment() {
        Long paymentId = payment.getId();
        CancelPaymentResponse response = paymentService.cancelPayment(paymentId,
                new CancelPaymentRequest("Registro duplicado"));
        entityManager.flush();
        entityManager.clear();

        PaymentEntity stored = payments.findById(paymentId).orElseThrow();
        assertTrue(stored.isCancelled());
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
                registeredAt, PaymentMethod.BANK_TRANSFER));
        paymentService.cancelPayment(payment.getId(), new CancelPaymentRequest("Duplicado"));
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
        paymentService.cancelPayment(payment.getId(), new CancelPaymentRequest("Duplicado"));
        PaymentResponse created = paymentService.registerPayment(new CreatePaymentRequest(
                debt.getId(), new BigDecimal("45000.00"), PaymentMethod.CARD));
        entityManager.flush();
        entityManager.clear();

        assertNotNull(created.id());
        assertTrue(payments.findById(payment.getId()).orElseThrow().isCancelled());
        DebtBalanceResponse balance = debtService.findDebtBalanceById(debt.getId());
        assertMoney("45000", balance.totalPaid());
        assertMoney("5000.25", balance.remainingBalance());
    }

    @Test
    void historyShouldKeepCancelledAndActivePaymentsInStableOrder() {
        // Igual fecha: el identificador debe desempatar.
        PaymentEntity second = payments.save(new PaymentEntity(debt, new BigDecimal("100.00"),
                registeredAt, PaymentMethod.CARD));
        paymentService.cancelPayment(payment.getId(), new CancelPaymentRequest("Duplicado"));
        entityManager.flush();
        entityManager.clear();

        List<PaymentHistoryResponse> history = paymentService.findPaymentHistoryByDebtId(debt.getId());
        assertEquals(List.of(payment.getId(), second.getId()),
                history.stream().map(PaymentHistoryResponse::id).toList());
        assertEquals("Duplicado", history.getFirst().cancellationReason());
        assertNotNull(history.getFirst().cancelledAt());
        assertMoney("10000", history.getFirst().amount());
        assertNull(history.getLast().cancelledAt());
        assertNull(history.getLast().cancellationReason());
    }

    @Test
    void cancellingMissingPaymentShouldFail() {
        // El fixture genera IDs positivos; un ID negativo no puede existir aquí.
        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.cancelPayment(-1L, new CancelPaymentRequest("Error")));
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "Esperado " + expected + ", obtenido " + actual);
    }
}
