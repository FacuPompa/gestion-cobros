package com.facupompa.pellet.domain.service;

import com.facupompa.pellet.domain.dto.CreateCustomerRequest;
import com.facupompa.pellet.domain.dto.CustomerBalanceResponse;
import com.facupompa.pellet.domain.dto.CustomerResponse;
import com.facupompa.pellet.domain.exception.CustomerNotFoundException;
import com.facupompa.pellet.persistence.entity.CustomerEntity;
import com.facupompa.pellet.persistence.entity.DebtEntity;
import com.facupompa.pellet.persistence.entity.PaymentEntity;
import com.facupompa.pellet.persistence.repository.CustomerRepository;
import com.facupompa.pellet.persistence.repository.DebtRepository;
import com.facupompa.pellet.persistence.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final DebtRepository debtRepository;
    private final PaymentRepository paymentRepository;

    public CustomerService(CustomerRepository customerRepository, DebtRepository debtRepository, PaymentRepository paymentRepository) {
        this.customerRepository = customerRepository;
        this.debtRepository = debtRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest createCustomerRequest) {
        CustomerEntity customerEntity = new CustomerEntity(createCustomerRequest.name());

        CustomerEntity savedCustomer = customerRepository.save(customerEntity);

        return new CustomerResponse(savedCustomer.getId(), savedCustomer.getName());
    }

    @Transactional(readOnly = true)
    public CustomerResponse findCustomerById(Long customerId) {
        Optional<CustomerEntity> customer = customerRepository.findById(customerId);

        CustomerEntity foundCustomer = customer.orElseThrow(() -> {
            return new CustomerNotFoundException(customerId);
        });
        return new CustomerResponse(foundCustomer.getId(), foundCustomer.getName());
    }

    @Transactional(readOnly = true)
    public CustomerBalanceResponse findCustomerBalanceById(Long customerId) {
        CustomerEntity customer = customerRepository.findById(customerId).orElseThrow(() -> {
            return new CustomerNotFoundException(customerId);
        });

        List<DebtEntity> debts = debtRepository.findAllByCustomerId(customerId);

        BigDecimal totalOriginalAmount = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (DebtEntity debt : debts) {
            totalOriginalAmount = debt.getOriginalAmount().add(totalOriginalAmount);

            List<PaymentEntity> payments = paymentRepository.findAllByDebtId(debt.getId());

            for (PaymentEntity payment : payments) {
                if(!payment.isCancelled()) {
                    totalPaid = payment.getAmount().add(totalPaid);
                }
            }
        }

        BigDecimal remainingBalance = totalOriginalAmount.subtract(totalPaid);

        return new CustomerBalanceResponse(
                customer.getId(),
                totalOriginalAmount,
                totalPaid,
                remainingBalance
        );


    }
}
