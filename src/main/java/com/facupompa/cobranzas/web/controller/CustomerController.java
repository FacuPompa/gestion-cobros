package com.facupompa.cobranzas.web.controller;

import com.facupompa.cobranzas.domain.dto.CreateCustomerRequest;
import com.facupompa.cobranzas.domain.dto.CustomerResponse;
import com.facupompa.cobranzas.domain.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}")
    ResponseEntity<CustomerResponse> findCustomerById(@PathVariable Long customerId) {
        CustomerResponse customerResponse = customerService.findCustomerById(customerId);

        return new ResponseEntity<>(customerResponse, HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest createCustomerRequest) {
        CustomerResponse customer = customerService.createCustomer(createCustomerRequest);
        return new ResponseEntity<>(customer, HttpStatus.CREATED);
    }
}
