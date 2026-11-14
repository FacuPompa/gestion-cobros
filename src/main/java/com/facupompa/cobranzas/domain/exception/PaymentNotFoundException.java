package com.facupompa.cobranzas.domain.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long paymentId) {
        super("Cobro con el id " + paymentId + " no encontrado");
    }
}
