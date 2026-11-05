package com.facupompa.cobranzas.domain.exception;

public class PaymentExceedsBalanceException extends RuntimeException {
    public PaymentExceedsBalanceException() {
        super("El importe del cobro supera el saldo pendiente de la deuda");
    }
}
