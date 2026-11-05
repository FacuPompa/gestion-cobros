package com.facupompa.cobranzas.domain.dto;

import com.facupompa.cobranzas.domain.model.PaymentMethod;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull
        @Positive
        Long debtId,

        @NotNull
        @Positive
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @NotNull
        PaymentMethod paymentMethod
) {
}
