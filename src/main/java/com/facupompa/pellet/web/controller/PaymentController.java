package com.facupompa.pellet.web.controller;

import com.facupompa.pellet.domain.dto.*;
import com.facupompa.pellet.domain.service.PaymentService;
import com.facupompa.pellet.web.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController{

    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    public PaymentController(PaymentService paymentService, CurrentUserService currentUserService) {
        this.paymentService = paymentService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    ResponseEntity<PaymentResponse> registerPayment (@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse paymentResponse = paymentService.registerPayment(request, currentUserService.getUsername());

        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }

    @PostMapping("/{paymentId}/cancel")
    ResponseEntity<CancelPaymentResponse> cancelPayment (@PathVariable Long paymentId, @Valid @RequestBody CancelPaymentRequest request) {
        CancelPaymentResponse cancelPaymentResponse = paymentService.cancelPayment(paymentId, request, currentUserService.getUsername());

        return new ResponseEntity<>(cancelPaymentResponse, HttpStatus.OK);
    }

    @GetMapping("/debt/{debtId}/history")
    ResponseEntity<List<PaymentHistoryResponse>> findPaymentHistoryByDebtId (@PathVariable Long debtId) {
        List<PaymentHistoryResponse> history = paymentService.findPaymentHistoryByDebtId(debtId);

        return new ResponseEntity<>(history, HttpStatus.OK);
    }
}
