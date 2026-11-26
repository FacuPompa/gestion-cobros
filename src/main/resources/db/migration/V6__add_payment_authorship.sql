ALTER TABLE payments
    ADD COLUMN registered_by VARCHAR(100),
    ADD COLUMN cancelled_by VARCHAR(100);
