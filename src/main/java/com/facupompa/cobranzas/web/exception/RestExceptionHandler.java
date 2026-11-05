package com.facupompa.cobranzas.web.exception;

import com.facupompa.cobranzas.domain.exception.CustomerNotFoundException;
import com.facupompa.cobranzas.domain.exception.DebtNotFoundException;
import com.facupompa.cobranzas.domain.exception.PaymentExceedsBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
