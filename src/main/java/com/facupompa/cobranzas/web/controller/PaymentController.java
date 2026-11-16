package com.facupompa.cobranzas.web.controller;

import com.facupompa.cobranzas.domain.dto.*;
import com.facupompa.cobranzas.domain.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/{paymentId}/cancel")
    ResponseEntity<CancelPaymentResponse> cancelPayment (@PathVariable Long paymentId, @Valid @RequestBody CancelPaymentRequest request) {
        CancelPaymentResponse cancelPaymentResponse = paymentService.cancelPayment(paymentId, request);

        return new ResponseEntity<>(cancelPaymentResponse, HttpStatus.OK);
    }

    @GetMapping("/debt/{debtId}/history")
    ResponseEntity<List<PaymentHistoryResponse>> findPaymentHistoryByDebtId (@PathVariable Long debtId) {
        List<PaymentHistoryResponse> history = paymentService.findPaymentHistoryByDebtId(debtId);

        return new ResponseEntity<>(history, HttpStatus.OK);
    }
}
