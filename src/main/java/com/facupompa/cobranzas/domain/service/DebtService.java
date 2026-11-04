package com.facupompa.cobranzas.domain.service;

import com.facupompa.cobranzas.domain.dto.CreateDebtRequest;
import com.facupompa.cobranzas.domain.dto.DebtResponse;
import com.facupompa.cobranzas.domain.exception.CustomerNotFoundException;
import com.facupompa.cobranzas.persistence.entity.CustomerEntity;
import com.facupompa.cobranzas.persistence.entity.DebtEntity;
import com.facupompa.cobranzas.persistence.repository.CustomerRepository;
import com.facupompa.cobranzas.persistence.repository.DebtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class DebtService {
    private final CustomerRepository customerRepository;
    private final DebtRepository debtRepository;

    public DebtService(CustomerRepository customerRepository, DebtRepository debtRepository) {
        this.customerRepository = customerRepository;
        this.debtRepository = debtRepository;
    }

    @Transactional
    public DebtResponse registerDebt(CreateDebtRequest request) {
        CustomerEntity customer = customerRepository.findById(request.customerId()).orElseThrow(() -> {
            return new CustomerNotFoundException(request.customerId());
        });

        LocalDate dueDate = request.dueDate();

        if (dueDate == null) {
            ZoneId zone = ZoneId.of("America/Argentina/Buenos_Aires");
            LocalDate today = LocalDate.now(zone);
            dueDate = today.plusMonths(1);
        }

        DebtEntity debt = new DebtEntity(customer, request.description(), request.originalAmount(), dueDate);
        DebtEntity savedDebt = debtRepository.save(debt);

        return new DebtResponse(savedDebt.getId(), savedDebt.getCustomer().getId(), savedDebt.getDescription(), savedDebt.getOriginalAmount(), savedDebt.getDueDate());
    }

    @Transactional(readOnly = true)
    public List<DebtResponse> findDebtsByCustomerId (Long customerId) {
        CustomerEntity customer = customerRepository.findById(customerId).orElseThrow(() ->{
            return new CustomerNotFoundException(customerId);
        } );

        List<DebtEntity> debts = debtRepository.findAllByCustomerId(customerId);

        List<DebtResponse> responses = new ArrayList<>();
        for (DebtEntity debt : debts) {
            DebtResponse response = new DebtResponse(
                    debt.getId(),
                    debt.getCustomer().getId(),
                    debt.getDescription(),
                    debt.getOriginalAmount(),
                    debt.getDueDate()
            );
            responses.add(response);
        }
        return responses;
    }
}
