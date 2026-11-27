package com.facupompa.cobranzas.domain.dto;

import java.util.List;

public record DebtPageResponse(
        List<DebtResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
