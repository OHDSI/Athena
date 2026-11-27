CREATE TABLE account_request_limits (
    scope varchar(32) NOT NULL,
    subject_hash varchar(64) NOT NULL,
    attempts bigint NOT NULL,
    expires_at timestamptz NOT NULL,
    PRIMARY KEY (scope, subject_hash)
);

CREATE INDEX account_request_limits_expiry_idx ON account_request_limits (expires_at);
