package com.facupompa.cobranzas.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebtResponse(Long id, Long customerId, String description, BigDecimal originalAmount, LocalDate dueDate) {
}
