package com.facupompa.pellet.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelPaymentRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
