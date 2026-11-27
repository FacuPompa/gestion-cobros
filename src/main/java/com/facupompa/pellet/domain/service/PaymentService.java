package com.facupompa.pellet.domain.service;

import com.facupompa.pellet.domain.dto.*;
import com.facupompa.pellet.domain.exception.DebtNotFoundException;
import com.facupompa.pellet.domain.exception.PaymentExceedsBalanceException;
import com.facupompa.pellet.domain.exception.PaymentNotFoundException;
import com.facupompa.pellet.persistence.entity.DebtEntity;
import com.facupompa.pellet.persistence.entity.PaymentEntity;
import com.facupompa.pellet.persistence.repository.DebtRepository;
import com.facupompa.pellet.persistence.repository.PaymentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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
    public PaymentResponse registerPayment(CreatePaymentRequest request, String registeredBy) {
        DebtEntity debt = debtRepository.findById(request.debtId()).orElseThrow(() -> {
            return new DebtNotFoundException(request.debtId());
        });

        List<PaymentEntity> payments = paymentRepository.findAllByDebtId(debt.getId());
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (PaymentEntity payment : payments) {
            if(!payment.isCancelled()) {
                totalPaid = totalPaid.add(payment.getAmount());
            }
        }

        BigDecimal remainingBalance = debt.getOriginalAmount().subtract(totalPaid);

        if (request.amount().compareTo(remainingBalance) > 0) {
            throw new PaymentExceedsBalanceException();
        }
        PaymentEntity payment = new PaymentEntity(
                debt, request.amount(), Instant.now(), request.paymentMethod(), registeredBy
        );

        PaymentEntity savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(savedPayment.getId(), savedPayment.getDebt().getId(), savedPayment.getAmount(), savedPayment.getPaymentMethod(), savedPayment.getRegisteredAt(), savedPayment.getRegisteredBy());
    }

    @Transactional
    public CancelPaymentResponse cancelPayment(Long paymentId, CancelPaymentRequest request, String cancelledBy) {
        PaymentEntity payment = paymentRepository.findById(paymentId).orElseThrow(() -> {
            return new PaymentNotFoundException(paymentId);
        });

        payment.cancel(request.reason(), Instant.now(), cancelledBy);

        return new CancelPaymentResponse(
                payment.getId(),
                payment.getDebt().getId(),
                payment.getAmount(),
                payment.getCancelledAt(),
                payment.getCancellationReason(),
                payment.getCancelledBy()
        );
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> findPaymentHistoryByDebtId(Long debtId){
        DebtEntity debt = debtRepository.findById(debtId).orElseThrow(() -> {
            return new DebtNotFoundException(debtId);
        });

        List<PaymentEntity> payments = paymentRepository.findAllByDebtIdOrderByRegisteredAtAscIdAsc(debt.getId());

        List<PaymentHistoryResponse> responses = new ArrayList<>();

        for (PaymentEntity payment : payments) {
            PaymentHistoryResponse response = new PaymentHistoryResponse(
                    payment.getId(),
                    payment.getDebt().getId(),
                    payment.getAmount(),
                    payment.getPaymentMethod(),
                    payment.getRegisteredAt(),
                    payment.getCancelledAt(),
                    payment.getCancellationReason(),
                    payment.getRegisteredBy(),
                    payment.getCancelledBy()
            );
            responses.add(response);
        }
        return responses;
    }
}
