package com.facupompa.pellet.persistence.repository;

import com.facupompa.pellet.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findAllByDebtId(Long debtId);
    List<PaymentEntity> findAllByDebtIdOrderByRegisteredAtAscIdAsc(Long debtId);
}
