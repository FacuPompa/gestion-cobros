package com.facupompa.pellet.web.controller;

import com.facupompa.pellet.domain.dto.CreateDebtRequest;
import com.facupompa.pellet.domain.dto.DebtBalanceResponse;
import com.facupompa.pellet.domain.dto.DebtResponse;
import com.facupompa.pellet.domain.service.DebtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.facupompa.pellet.domain.dto.DebtPageResponse;

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
    ResponseEntity<DebtPageResponse> findDebtsByCustomerId(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        DebtPageResponse debts = debtService.findDebtsByCustomerId(customerId, page, size);

        return  new ResponseEntity<>(debts, HttpStatus.OK);
    }

    @GetMapping("/{debtId}/balance")
    ResponseEntity<DebtBalanceResponse> findDebtBalanceById(@PathVariable Long debtId){
        DebtBalanceResponse debtBalanceResponse = debtService.findDebtBalanceById(debtId);

        return new ResponseEntity<>(debtBalanceResponse, HttpStatus.OK);
    }
}
