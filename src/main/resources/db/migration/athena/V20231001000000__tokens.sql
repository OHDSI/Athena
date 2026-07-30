CREATE TABLE tokens (
  id         BIGINT PRIMARY KEY,
  user_id    BIGINT NOT NULL REFERENCES users(id),
  value      VARCHAR(1024)
);

CREATE SEQUENCE tokens_seq START WITH 1000;
