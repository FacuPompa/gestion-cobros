package com.facupompa.cobranzas.domain.service;

import com.facupompa.cobranzas.domain.dto.CreatePaymentRequest;
import com.facupompa.cobranzas.domain.dto.PaymentResponse;
import com.facupompa.cobranzas.domain.exception.DebtNotFoundException;
import com.facupompa.cobranzas.domain.exception.PaymentExceedsBalanceException;
import com.facupompa.cobranzas.persistence.entity.DebtEntity;
import com.facupompa.cobranzas.persistence.entity.PaymentEntity;
import com.facupompa.cobranzas.persistence.repository.DebtRepository;
import com.facupompa.cobranzas.persistence.repository.PaymentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class PaymentService {

    private final DebtRepository debtRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(DebtRepository debtRepository, PaymentRepository paymentRepository) {
        this.debtRepository = debtRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse registerPayment(CreatePaymentRequest request) {
        DebtEntity debt = debtRepository.findById(request.debtId()).orElseThrow(() -> {
            return new DebtNotFoundException(request.debtId());
        });

        List<PaymentEntity> payments = paymentRepository.findAllByDebtId(debt.getId());
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (PaymentEntity payment : payments) {
            totalPaid = totalPaid.add(payment.getAmount());
        }

        BigDecimal remainingBalance = debt.getOriginalAmount().subtract(totalPaid);

        if (request.amount().compareTo(remainingBalance) > 0) {
            throw new PaymentExceedsBalanceException();
        }
        PaymentEntity payment = new PaymentEntity(
                debt, request.amount(), Instant.now(), request.paymentMethod()
        );

        PaymentEntity savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(savedPayment.getId(), savedPayment.getDebt().getId(), savedPayment.getAmount(), savedPayment.getPaymentMethod(), savedPayment.getRegisteredAt());
    }
}
