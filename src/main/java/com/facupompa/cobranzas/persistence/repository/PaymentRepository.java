package com.facupompa.cobranzas.persistence.repository;

import com.facupompa.cobranzas.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findAllByDebtId(Long debtId);
}
