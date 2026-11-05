package com.facupompa.cobranzas.domain.dto;

import com.facupompa.cobranzas.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long debtId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Instant registeredAt
) {
}
