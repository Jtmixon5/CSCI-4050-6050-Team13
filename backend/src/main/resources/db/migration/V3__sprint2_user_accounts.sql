CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(30) NOT NULL,
    account_status VARCHAR(30) NOT NULL,

    promotion_opt_in BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),

    CONSTRAINT chk_users_role CHECK (
        role IN ('CUSTOMER', 'ADMIN')
    ),

    CONSTRAINT chk_users_account_status CHECK (
        account_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')
    )
);
CREATE TABLE email_verification_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    token_hash VARCHAR(255) NOT NULL,

    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_email_verification_tokens PRIMARY KEY (id),

    CONSTRAINT fk_email_verification_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_email_verification_tokens_token_hash
        UNIQUE (token_hash)
);
CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    token_hash VARCHAR(255) NOT NULL,

    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),

    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_password_reset_tokens_token_hash
        UNIQUE (token_hash)
);
CREATE TABLE user_addresses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_addresses PRIMARY KEY (id),

    CONSTRAINT fk_user_addresses_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_user_addresses_user
        UNIQUE (user_id)
);
CREATE TABLE payment_cards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    card_slot INT NOT NULL,

    cardholder_name VARCHAR(150) NOT NULL,
    card_type VARCHAR(50) NOT NULL,

    last_four VARCHAR(4) NOT NULL,

    encrypted_card_number VARCHAR(1000) NOT NULL,
    encrypted_expiration_month VARCHAR(1000) NOT NULL,
    encrypted_expiration_year VARCHAR(1000) NOT NULL,

    billing_zip_code VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payment_cards PRIMARY KEY (id),

    CONSTRAINT fk_payment_cards_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_payment_cards_slot CHECK (
        card_slot BETWEEN 1 AND 3
    ),

    CONSTRAINT uq_payment_cards_user_slot
        UNIQUE (user_id, card_slot)
);
CREATE TABLE favorite_movies (
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_favorite_movies PRIMARY KEY (user_id, movie_id),

    CONSTRAINT fk_favorite_movies_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_favorite_movies_movie
        FOREIGN KEY (movie_id)
        REFERENCES movies(id)
        ON DELETE CASCADE
);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_account_status ON users(account_status);

CREATE INDEX idx_email_verification_tokens_user_id
    ON email_verification_tokens(user_id);

CREATE INDEX idx_password_reset_tokens_user_id
    ON password_reset_tokens(user_id);

CREATE INDEX idx_payment_cards_user_id
    ON payment_cards(user_id);

CREATE INDEX idx_favorite_movies_movie_id
    ON favorite_movies(movie_id);