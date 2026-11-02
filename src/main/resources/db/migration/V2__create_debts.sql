CREATE TABLE debts (
                       id BIGINT GENERATED ALWAYS AS IDENTITY,
                       customer_id BIGINT NOT NULL,
                       description VARCHAR(255) NOT NULL,
                       original_amount NUMERIC(19, 2) NOT NULL,
                       due_date DATE NOT NULL,

                       CONSTRAINT pk_debts PRIMARY KEY (id),
                       CONSTRAINT fk_debts_customer
                           FOREIGN KEY (customer_id) REFERENCES customers (id),
                       CONSTRAINT ck_debts_original_amount_positive
                           CHECK (original_amount > 0)
);