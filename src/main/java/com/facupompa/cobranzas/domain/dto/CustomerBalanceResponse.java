package com.facupompa.cobranzas.domain.dto;

import java.math.BigDecimal;

public record CustomerBalanceResponse(
        Long customerId,
        BigDecimal totalOriginalAmount,
        BigDecimal totalPaid,
        BigDecimal remainingBalance
) {
}
