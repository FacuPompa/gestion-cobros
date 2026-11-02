package com.facupompa.cobranzas.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "debts")
@Getter
@NoArgsConstructor
public class DebtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, name = "original_amount", precision = 19, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    public DebtEntity(CustomerEntity customer, String description, BigDecimal originalAmount, LocalDate dueDate) {
        this.customer = customer;
        this.description = description;
        this.originalAmount = originalAmount;
        this.dueDate = dueDate;
    }
}
