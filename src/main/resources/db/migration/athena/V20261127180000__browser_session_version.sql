-- Older signed browser tokens have implicit version zero.
ALTER TABLE users ADD COLUMN browser_session_version bigint NOT NULL DEFAULT 0;
