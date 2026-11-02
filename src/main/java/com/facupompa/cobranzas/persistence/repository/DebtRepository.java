package com.facupompa.cobranzas.persistence.repository;

import com.facupompa.cobranzas.persistence.entity.DebtEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebtRepository extends JpaRepository<DebtEntity, Long> {
}
