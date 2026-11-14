package com.facupompa.cobranzas.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CancelPaymentResponse(
        Long paymentId,
        Long debtId,
        BigDecimal amount,
        Instant cancelledAt,
        String cancellationReason

) {
}
