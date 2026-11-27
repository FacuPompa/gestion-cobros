package com.facupompa.pellet.domain.service;

import com.facupompa.pellet.domain.dto.CreateDebtRequest;
import com.facupompa.pellet.domain.dto.DebtBalanceResponse;
import com.facupompa.pellet.domain.dto.DebtPageResponse;
import com.facupompa.pellet.domain.dto.DebtResponse;
import com.facupompa.pellet.domain.exception.CustomerNotFoundException;
import com.facupompa.pellet.domain.exception.DebtNotFoundException;
import com.facupompa.pellet.persistence.entity.CustomerEntity;
import com.facupompa.pellet.persistence.entity.DebtEntity;
import com.facupompa.pellet.persistence.entity.PaymentEntity;
import com.facupompa.pellet.persistence.repository.CustomerRepository;
import com.facupompa.pellet.persistence.repository.DebtRepository;
import com.facupompa.pellet.persistence.repository.PaymentRepository;
import com.facupompa.pellet.domain.exception.InvalidPaginationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class DebtService {
    private final CustomerRepository customerRepository;
    private final DebtRepository debtRepository;
    private final PaymentRepository paymentRepository;

    public DebtService(CustomerRepository customerRepository, DebtRepository debtRepository, PaymentRepository paymentRepository) {
        this.customerRepository = customerRepository;
        this.debtRepository = debtRepository;
        this.paymentRepository = paymentRepository;
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
    public DebtPageResponse findDebtsByCustomerId (Long customerId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidPaginationException();
        }
        CustomerEntity customer = customerRepository.findById(customerId).orElseThrow(() ->{
            return new CustomerNotFoundException(customerId);
        } );

        Page<DebtEntity> debts = debtRepository.findAllByCustomerId(customerId,
                PageRequest.of(page, size, Sort.by("dueDate", "id")));

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
        return new DebtPageResponse(responses, debts.getNumber(), debts.getSize(),
                debts.getTotalElements(), debts.getTotalPages(), debts.isLast());
    }

    @Transactional(readOnly = true)
    public DebtBalanceResponse findDebtBalanceById(Long debtId) {
        DebtEntity debt = debtRepository.findById(debtId).orElseThrow(() -> {
            return new DebtNotFoundException(debtId);
        });

        List<PaymentEntity> payments = paymentRepository.findAllByDebtId(debtId);

        BigDecimal totalPaid = BigDecimal.ZERO;

        for(PaymentEntity payment : payments) {
            if(!payment.isCancelled()) {
                totalPaid = totalPaid.add(payment.getAmount());
            }
        }

        BigDecimal remainingBalance = debt.getOriginalAmount().subtract(totalPaid);

        return new DebtBalanceResponse(
                debt.getId(),
                debt.getOriginalAmount(),
                totalPaid,
                remainingBalance
        );
    }
}
