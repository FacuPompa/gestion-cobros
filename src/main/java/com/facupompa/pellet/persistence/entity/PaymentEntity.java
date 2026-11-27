package com.facupompa.pellet.persistence.entity;

import com.facupompa.pellet.domain.exception.PaymentAlreadyCancelledException;
import com.facupompa.pellet.domain.model.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "debt_id", nullable = false)
    private DebtEntity debt;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "registered_by", updatable = false, length = 100)
    private String registeredBy;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    public PaymentEntity(DebtEntity debt, BigDecimal amount, Instant registeredAt, PaymentMethod paymentMethod, String registeredBy) {
        this.debt = debt;
        this.amount = amount;
        this.registeredAt = registeredAt;
        this.paymentMethod = paymentMethod;
        this.registeredBy = registeredBy;
    }

    public boolean isCancelled() {
        return cancelledAt != null;
    }

    public void cancel(String reason, Instant cancelledAt, String cancelledBy){
        if (isCancelled()) {
            throw new PaymentAlreadyCancelledException(id);
        }

        this.cancellationReason = reason;
        this.cancelledAt = cancelledAt;
        this.cancelledBy = cancelledBy;
    }
}
