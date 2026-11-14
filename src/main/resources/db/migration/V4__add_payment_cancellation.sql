ALTER TABLE payments
    ADD COLUMN cancelled_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN cancellation_reason VARCHAR(255);
