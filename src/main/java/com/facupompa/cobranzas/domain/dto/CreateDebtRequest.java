package com.facupompa.cobranzas.domain.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDebtRequest(@NotNull @Positive Long customerId, @NotBlank @Size(max = 255) String description , @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal originalAmount, LocalDate dueDate) {
}
