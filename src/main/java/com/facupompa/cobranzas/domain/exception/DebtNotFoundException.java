package com.facupompa.cobranzas.domain.exception;

public class DebtNotFoundException extends RuntimeException {
    public DebtNotFoundException(Long debtId) {
        super("Deuda con el id " + debtId + " no encontrada");
    }
}
