package com.facupompa.pellet.domain.dto;

import com.facupompa.pellet.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long debtId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Instant registeredAt,
        String registeredBy
) {
}
