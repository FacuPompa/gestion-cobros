package com.facupompa.cobranzas.web.controller;

import com.facupompa.cobranzas.domain.dto.CreateDebtRequest;
import com.facupompa.cobranzas.domain.dto.DebtBalanceResponse;
import com.facupompa.cobranzas.domain.dto.DebtResponse;
import com.facupompa.cobranzas.domain.service.DebtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/customer/{customerId}")
    ResponseEntity<List<DebtResponse>> findDebtsByCustomerId(@PathVariable Long customerId) {
        List<DebtResponse> debts = debtService.findDebtsByCustomerId(customerId);

        return  new ResponseEntity<>(debts, HttpStatus.OK);
    }

    @GetMapping("/{debtId}/balance")
    ResponseEntity<DebtBalanceResponse> findDebtBalanceById(@PathVariable Long debtId){
        DebtBalanceResponse debtBalanceResponse = debtService.findDebtBalanceById(debtId);

        return new ResponseEntity<>(debtBalanceResponse, HttpStatus.OK);
    }
}
