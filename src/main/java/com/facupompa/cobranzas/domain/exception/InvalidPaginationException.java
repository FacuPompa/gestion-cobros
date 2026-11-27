package com.facupompa.cobranzas.domain.exception;

public class InvalidPaginationException extends RuntimeException {
    public InvalidPaginationException() {
        super("La página debe ser mayor o igual a 0 y el tamaño debe estar entre 1 y 100");
    }
}
