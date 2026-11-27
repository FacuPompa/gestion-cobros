package com.facupompa.cobranzas.web.exception;

import com.facupompa.cobranzas.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiError> handleCustomerNotFound(CustomerNotFoundException exception) {
        ApiError error = new ApiError("customer-not-found", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DebtNotFoundException.class)
    ResponseEntity<ApiError> handleDebtNotFound (DebtNotFoundException exception) {
        ApiError error = new ApiError("debt-not-found", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PaymentExceedsBalanceException.class)
    ResponseEntity<ApiError> handlePaymentExceedsBalance (PaymentExceedsBalanceException exception) {
        ApiError error = new ApiError("payment-exceeds-balance", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error); // codigo 409
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    ResponseEntity<ApiError> handlePaymentNotFound(PaymentNotFoundException exception) {
        ApiError error = new ApiError("payment-not-found", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PaymentAlreadyCancelledException.class)
    ResponseEntity<ApiError> handlePaymentAlreadyCancelled(PaymentAlreadyCancelledException exception) {
        ApiError error = new ApiError("payment-already-cancelled", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> handleAuthentication(AuthenticationException exception) {
        ApiError error = new ApiError("invalid-credentials", "Usuario o contrasenia incorrectos");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidPaginationException.class)
    ResponseEntity<ApiError> handleInvalidPagination(InvalidPaginationException exception) {
        ApiError error = new ApiError("invalid-pagination", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
