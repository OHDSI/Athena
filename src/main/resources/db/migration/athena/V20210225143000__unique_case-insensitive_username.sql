ALTER TABLE users DROP CONSTRAINT IF EXISTS users_login_origin_uq;
CREATE UNIQUE INDEX IF NOT EXISTS users_login_origin_uq ON users (lower(login),origin);