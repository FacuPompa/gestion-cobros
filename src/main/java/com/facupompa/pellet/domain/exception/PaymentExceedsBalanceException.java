package com.facupompa.pellet.domain.exception;

public class PaymentExceedsBalanceException extends RuntimeException {
    public PaymentExceedsBalanceException() {
        super("El importe del cobro supera el saldo pendiente de la deuda");
    }
}
