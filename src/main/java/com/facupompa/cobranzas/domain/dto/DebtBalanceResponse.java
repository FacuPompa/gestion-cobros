package com.facupompa.cobranzas.domain.dto;

import java.math.BigDecimal;

public record DebtBalanceResponse(
        Long debtId,
        BigDecimal originalAmount,
        BigDecimal totalPaid,
        BigDecimal remainingBalance
) {
}
