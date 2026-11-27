-- The Athena ID owns licenses, downloads and roles. A future credential import
-- can record the source directory ID without changing that ownership.
CREATE TABLE users_data (
    athena_user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    arachne_user_id BIGINT UNIQUE,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(512) NOT NULL,
    last_password_reset TIMESTAMPTZ,
    email_confirmed_at TIMESTAMPTZ,
    confirmation_token_hash VARCHAR(64) UNIQUE,
    confirmation_expires_at TIMESTAMPTZ,
    department VARCHAR(255),
    professional_type_id BIGINT REFERENCES professional_types(id),
    country_id BIGINT REFERENCES countries(id),
    state_province_id BIGINT REFERENCES states_provinces(id),
    address1 TEXT,
    address2 TEXT,
    city TEXT,
    zip_code TEXT,
    phone TEXT,
    mobile TEXT,
    contact_email VARCHAR(255),
    CHECK ((confirmation_token_hash IS NULL) = (confirmation_expires_at IS NULL)),
    CHECK (email_confirmed_at IS NULL OR confirmation_token_hash IS NULL)
);

CREATE UNIQUE INDEX users_data_email_ci ON users_data (lower(email));
