package com.facupompa.cobranzas.web.controller;

import com.facupompa.cobranzas.domain.dto.CreateDebtRequest;
import com.facupompa.cobranzas.domain.dto.DebtResponse;
import com.facupompa.cobranzas.domain.service.DebtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @PostMapping
    ResponseEntity<DebtResponse> registerDebt(@Valid @RequestBody CreateDebtRequest createDebtRequest) {
        DebtResponse debtResponse = debtService.registerDebt(createDebtRequest);

        return new ResponseEntity<>(debtResponse, HttpStatus.CREATED);
    }
}
