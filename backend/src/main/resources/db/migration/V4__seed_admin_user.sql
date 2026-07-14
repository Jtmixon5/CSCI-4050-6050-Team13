INSERT INTO users (
    first_name,
    last_name,
    email,
    phone_number,
    password_hash,
    role,
    account_status,
    promotion_opt_in
) VALUES (
    'Admin',
    'User',
    'admin@cinema.com',
    '000-000-0000',
    '$2a$10$REPLACE_THIS_WITH_A_REAL_BCRYPT_HASH',
    'ADMIN',
    'ACTIVE',
    FALSE
);