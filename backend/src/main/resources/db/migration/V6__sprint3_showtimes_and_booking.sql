CREATE TABLE showrooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_showrooms PRIMARY KEY (id),
    CONSTRAINT uq_showrooms_name UNIQUE (name),
    CONSTRAINT chk_showrooms_capacity CHECK (capacity > 0)
);

CREATE TABLE seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    showroom_id BIGINT NOT NULL,
    row_label VARCHAR(5) NOT NULL,
    seat_number INT NOT NULL,
    is_accessible BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_seats PRIMARY KEY (id),
    CONSTRAINT uq_seats_id_showroom UNIQUE (id, showroom_id),
    CONSTRAINT uq_seats_showroom_position
        UNIQUE (showroom_id, row_label, seat_number),
    CONSTRAINT chk_seats_number CHECK (seat_number > 0),
    CONSTRAINT fk_seats_showroom
        FOREIGN KEY (showroom_id)
        REFERENCES showrooms(id)
        ON DELETE CASCADE
);

CREATE TABLE showtimes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    movie_id BIGINT NOT NULL,
    showroom_id BIGINT NOT NULL,
    starts_at DATETIME NOT NULL,
    ends_at DATETIME NOT NULL,
    adult_price DECIMAL(8, 2) NOT NULL,
    child_price DECIMAL(8, 2) NOT NULL,
    senior_price DECIMAL(8, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_showtimes PRIMARY KEY (id),
    CONSTRAINT uq_showtimes_id_showroom UNIQUE (id, showroom_id),
    CONSTRAINT uq_showtimes_showroom_start
        UNIQUE (showroom_id, starts_at),
    CONSTRAINT chk_showtimes_time_range CHECK (ends_at > starts_at),
    CONSTRAINT chk_showtimes_prices CHECK (
        adult_price >= 0
        AND child_price >= 0
        AND senior_price >= 0
    ),
    CONSTRAINT chk_showtimes_status CHECK (
        status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED')
    ),
    CONSTRAINT fk_showtimes_movie
        FOREIGN KEY (movie_id)
        REFERENCES movies(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_showtimes_showroom
        FOREIGN KEY (showroom_id)
        REFERENCES showrooms(id)
        ON DELETE RESTRICT
);

CREATE TABLE bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    showtime_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    session_token VARCHAR(128) NOT NULL,
    contact_email VARCHAR(255) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_bookings PRIMARY KEY (id),
    CONSTRAINT uq_bookings_id_showtime UNIQUE (id, showtime_id),
    CONSTRAINT uq_bookings_session_token UNIQUE (session_token),
    CONSTRAINT chk_bookings_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_bookings_status CHECK (
        status IN (
            'DRAFT',
            'CHECKOUT',
            'PAYMENT_PENDING',
            'CONFIRMED',
            'CANCELLED',
            'EXPIRED'
        )
    ),
    CONSTRAINT fk_bookings_showtime
        FOREIGN KEY (showtime_id)
        REFERENCES showtimes(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE TABLE booking_tickets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    ticket_type VARCHAR(20) NOT NULL,
    unit_price DECIMAL(8, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_booking_tickets PRIMARY KEY (id),
    CONSTRAINT uq_booking_tickets_id_booking UNIQUE (id, booking_id),
    CONSTRAINT chk_booking_tickets_type CHECK (
        ticket_type IN ('ADULT', 'CHILD', 'SENIOR')
    ),
    CONSTRAINT chk_booking_tickets_price CHECK (unit_price >= 0),
    CONSTRAINT fk_booking_tickets_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(id)
        ON DELETE CASCADE
);

CREATE TABLE seat_reservations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    showroom_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_seat_reservations PRIMARY KEY (id),
    CONSTRAINT uq_seat_reservations_showtime_seat
        UNIQUE (showtime_id, seat_id),
    CONSTRAINT uq_seat_reservations_booking_seat
        UNIQUE (booking_id, seat_id),
    CONSTRAINT uq_seat_reservations_ticket UNIQUE (ticket_id),
    CONSTRAINT fk_seat_reservations_booking_showtime
        FOREIGN KEY (booking_id, showtime_id)
        REFERENCES bookings(id, showtime_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_seat_reservations_showtime_showroom
        FOREIGN KEY (showtime_id, showroom_id)
        REFERENCES showtimes(id, showroom_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_seat_reservations_seat_showroom
        FOREIGN KEY (seat_id, showroom_id)
        REFERENCES seats(id, showroom_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_seat_reservations_ticket
        FOREIGN KEY (ticket_id, booking_id)
        REFERENCES booking_tickets(id, booking_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_seats_showroom_id ON seats(showroom_id);
CREATE INDEX idx_showtimes_movie_start ON showtimes(movie_id, starts_at);
CREATE INDEX idx_showtimes_start ON showtimes(starts_at);
CREATE INDEX idx_bookings_showtime_id ON bookings(showtime_id);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_status_expires ON bookings(status, expires_at);
CREATE INDEX idx_booking_tickets_booking_id ON booking_tickets(booking_id);
CREATE INDEX idx_seat_reservations_booking_id
    ON seat_reservations(booking_id);
CREATE INDEX idx_seat_reservations_seat_showroom
    ON seat_reservations(seat_id, showroom_id);

INSERT INTO showrooms (name, capacity)
VALUES
    ('Showroom 1', 40),
    ('Showroom 2', 40),
    ('Showroom 3', 40);

INSERT INTO seats (
    showroom_id,
    row_label,
    seat_number,
    is_accessible
)
SELECT
    sr.id,
    seat_rows.row_label,
    seat_numbers.seat_number,
    seat_rows.row_label = 'A' AND seat_numbers.seat_number IN (1, 8)
FROM showrooms sr
CROSS JOIN (
    SELECT 'A' AS row_label
    UNION ALL SELECT 'B'
    UNION ALL SELECT 'C'
    UNION ALL SELECT 'D'
    UNION ALL SELECT 'E'
) seat_rows
CROSS JOIN (
    SELECT 1 AS seat_number
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
    UNION ALL SELECT 5
    UNION ALL SELECT 6
    UNION ALL SELECT 7
    UNION ALL SELECT 8
) seat_numbers
WHERE sr.name IN ('Showroom 1', 'Showroom 2', 'Showroom 3');

INSERT INTO showtimes (
    movie_id,
    showroom_id,
    starts_at,
    ends_at,
    adult_price,
    child_price,
    senior_price
)
SELECT
    movie_data.movie_id,
    sr.id,
    TIMESTAMP(
        DATE_ADD(CURRENT_DATE, INTERVAL movie_data.day_offset DAY),
        movie_data.start_time
    ),
    DATE_ADD(
        TIMESTAMP(
            DATE_ADD(CURRENT_DATE, INTERVAL movie_data.day_offset DAY),
            movie_data.start_time
        ),
        INTERVAL 150 MINUTE
    ),
    14.99,
    9.99,
    11.99
FROM (
    SELECT 1 AS movie_id, 'Showroom 1' AS showroom_name,
        1 AS day_offset, CAST('14:00:00' AS TIME) AS start_time
    UNION ALL
    SELECT 2, 'Showroom 2', 1, CAST('17:00:00' AS TIME)
    UNION ALL
    SELECT 3, 'Showroom 3', 1, CAST('20:00:00' AS TIME)
    UNION ALL
    SELECT 1, 'Showroom 2', 2, CAST('14:00:00' AS TIME)
    UNION ALL
    SELECT 2, 'Showroom 3', 2, CAST('17:00:00' AS TIME)
    UNION ALL
    SELECT 3, 'Showroom 1', 2, CAST('20:00:00' AS TIME)
) movie_data
JOIN showrooms sr
    ON sr.name = movie_data.showroom_name
JOIN movies m
    ON m.id = movie_data.movie_id;
