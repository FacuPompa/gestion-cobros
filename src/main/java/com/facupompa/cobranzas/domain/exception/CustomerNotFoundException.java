package com.facupompa.cobranzas.domain.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long customerId) {
        super("Cliente con el id " + customerId + " no encontrado");
    }
}
