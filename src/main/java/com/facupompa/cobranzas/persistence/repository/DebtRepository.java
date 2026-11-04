package com.facupompa.cobranzas.persistence.repository;

import com.facupompa.cobranzas.persistence.entity.DebtEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DebtRepository extends JpaRepository<DebtEntity, Long> {
    List<DebtEntity> findAllByCustomerId(Long customerId);
}
