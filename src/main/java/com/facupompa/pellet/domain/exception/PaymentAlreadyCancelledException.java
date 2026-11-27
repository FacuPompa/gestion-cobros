package com.facupompa.pellet.domain.exception;

public class PaymentAlreadyCancelledException extends RuntimeException {
    public PaymentAlreadyCancelledException(Long paymentId) {
        super("El cobro con el id " + paymentId + " ya está anulado");
    }
}
