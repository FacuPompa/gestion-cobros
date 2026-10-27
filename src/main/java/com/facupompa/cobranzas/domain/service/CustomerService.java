package com.facupompa.cobranzas.domain.service;

import com.facupompa.cobranzas.domain.dto.CreateCustomerRequest;
import com.facupompa.cobranzas.domain.dto.CustomerResponse;
import com.facupompa.cobranzas.persistence.entity.CustomerEntity;
import com.facupompa.cobranzas.persistence.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest createCustomerRequest) {
        CustomerEntity customerEntity = new CustomerEntity(createCustomerRequest.name());

        CustomerEntity savedCustomer = customerRepository.save(customerEntity);

        return new CustomerResponse(savedCustomer.getId(), savedCustomer.getName());
    }
}
