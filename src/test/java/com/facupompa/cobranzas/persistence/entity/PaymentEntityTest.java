package com.facupompa.cobranzas.persistence.entity;

import com.facupompa.cobranzas.domain.exception.PaymentAlreadyCancelledException;
import com.facupompa.cobranzas.domain.model.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PaymentEntityTest {
    private final Instant registeredAt = Instant.parse("2026-11-18T12:00:00Z");
    private final Instant cancelledAt = Instant.parse("2026-11-18T13:00:00Z");

    private PaymentEntity payment() {
        CustomerEntity customer = new CustomerEntity("Cliente de prueba");
        DebtEntity debt = new DebtEntity(customer, "Deuda de prueba",
                new BigDecimal("50000.00"), LocalDate.of(2026, 12, 18));
        return new PaymentEntity(debt, new BigDecimal("10000.00"), registeredAt, PaymentMethod.CASH, "operador");
    }

    @Test
    void newPaymentShouldBeActive() {
        PaymentEntity payment = payment();
        assertFalse(payment.isCancelled());
        assertEquals("operador", payment.getRegisteredBy());
        assertNull(payment.getCancelledBy());
        assertNull(payment.getCancelledAt());
        assertNull(payment.getCancellationReason());
    }

    @Test
    void cancelShouldPreserveOriginalPayment() {
        PaymentEntity payment = payment();
        DebtEntity originalDebt = payment.getDebt();

        payment.cancel("Registro duplicado", cancelledAt, "admin");

        assertTrue(payment.isCancelled());
        assertEquals("operador", payment.getRegisteredBy());
        assertEquals("admin", payment.getCancelledBy());
        assertEquals(cancelledAt, payment.getCancelledAt());
        assertEquals("Registro duplicado", payment.getCancellationReason());
        assertEquals(new BigDecimal("10000.00"), payment.getAmount());
        assertSame(originalDebt, payment.getDebt());
        assertEquals(registeredAt, payment.getRegisteredAt());
        assertEquals(PaymentMethod.CASH, payment.getPaymentMethod());
    }

    @Test
    void secondCancellationShouldFailWithoutOverwritingFirstCancellation() {
        PaymentEntity payment = payment();
        payment.cancel("Primer motivo", cancelledAt, "admin");

        assertThrows(PaymentAlreadyCancelledException.class,
                () -> payment.cancel("Otro motivo", cancelledAt.plusSeconds(60), "otro-admin"));

        assertEquals(cancelledAt, payment.getCancelledAt());
        assertEquals("Primer motivo", payment.getCancellationReason());
        assertEquals("admin", payment.getCancelledBy());
        assertEquals("operador", payment.getRegisteredBy());
        assertEquals(new BigDecimal("10000.00"), payment.getAmount());
    }
}
