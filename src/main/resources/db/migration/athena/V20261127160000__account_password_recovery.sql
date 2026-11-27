ALTER TABLE users_data
    ADD COLUMN reset_token_hash varchar(64) UNIQUE,
    ADD COLUMN reset_expires_at timestamptz,
    ADD CHECK ((reset_token_hash IS NULL) = (reset_expires_at IS NULL));
