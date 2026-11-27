package com.facupompa.pellet.domain.dto;

import java.math.BigDecimal;

public record DebtBalanceResponse(
        Long debtId,
        BigDecimal originalAmount,
        BigDecimal totalPaid,
        BigDecimal remainingBalance
) {
}
