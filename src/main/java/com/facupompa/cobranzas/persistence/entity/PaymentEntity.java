package com.facupompa.cobranzas.persistence.entity;

import com.facupompa.cobranzas.domain.model.PaymentMethod;
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

    public PaymentEntity(DebtEntity debt, BigDecimal amount, Instant registeredAt, PaymentMethod paymentMethod) {
        this.debt = debt;
        this.amount = amount;
        this.registeredAt = registeredAt;
        this.paymentMethod = paymentMethod;
    }
}
