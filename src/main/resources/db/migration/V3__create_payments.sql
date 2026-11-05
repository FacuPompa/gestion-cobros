CREATE TABLE payments (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    debt_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    payment_method VARCHAR(255) NOT NULL,

    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT fk_payments_debt
        FOREIGN KEY (debt_id) REFERENCES debts (id),
    CONSTRAINT ck_payments_amount_positive
        CHECK (amount > 0),
    CONSTRAINT ck_payments_payment_method
        CHECK (payment_method IN ('CASH', 'BANK_TRANSFER', 'CARD'))
);