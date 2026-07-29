ALTER TABLE bookings
    ADD COLUMN tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN confirmation_number VARCHAR(40) NULL,
    ADD COLUMN card_last_four VARCHAR(4) NULL,
    ADD COLUMN confirmed_at TIMESTAMP NULL;

ALTER TABLE bookings
    ADD CONSTRAINT uq_bookings_confirmation_number UNIQUE (confirmation_number),
    ADD CONSTRAINT chk_bookings_tax_amount CHECK (tax_amount >= 0),
    ADD CONSTRAINT chk_bookings_total_amount CHECK (total_amount >= 0);

CREATE INDEX idx_bookings_user_status_confirmed
    ON bookings (user_id, status, confirmed_at);
