package com.facupompa.pellet.domain.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long paymentId) {
        super("Cobro con el id " + paymentId + " no encontrado");
    }
}
