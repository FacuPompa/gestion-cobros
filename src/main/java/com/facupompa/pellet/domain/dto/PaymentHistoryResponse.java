package com.facupompa.pellet.domain.dto;

import com.facupompa.pellet.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentHistoryResponse(
        Long id,
        Long debtId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Instant registeredAt,
        Instant cancelledAt,
        String cancellationReason,
        String registeredBy,
        String cancelledBy
) {
}
