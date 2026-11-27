package com.facupompa.pellet.domain.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long customerId) {
        super("Cliente con el id " + customerId + " no encontrado");
    }
}
