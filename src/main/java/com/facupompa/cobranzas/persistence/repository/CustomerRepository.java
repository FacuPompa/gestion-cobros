package com.facupompa.cobranzas.persistence.repository;

import com.facupompa.cobranzas.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
}
