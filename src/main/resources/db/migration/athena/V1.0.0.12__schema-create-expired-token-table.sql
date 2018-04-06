CREATE TABLE revoked_tokens(
  id BIGSERIAL PRIMARY KEY,
  token VARCHAR(1024) NOT NULL,
  CONSTRAINT idx_expired_tokens_token UNIQUE(token)
);