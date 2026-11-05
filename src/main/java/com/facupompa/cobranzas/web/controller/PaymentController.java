package com.facupompa.cobranzas.web.controller;

import com.facupompa.cobranzas.domain.dto.CreatePaymentRequest;
import com.facupompa.cobranzas.domain.dto.PaymentResponse;
import com.facupompa.cobranzas.domain.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController{

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    ResponseEntity<PaymentResponse> registerPayment (@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse paymentResponse = paymentService.registerPayment(request);

        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }
}
