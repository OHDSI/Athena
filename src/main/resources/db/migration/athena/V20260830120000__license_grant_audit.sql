ALTER TABLE licenses ADD COLUMN granted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE licenses ADD COLUMN granted_by_user_id BIGINT;
ALTER TABLE licenses ADD COLUMN granted_by_name VARCHAR;

CREATE INDEX licenses_status_request_date_idx
    ON licenses (status, request_date DESC);

CREATE INDEX licenses_user_activity_date_idx
    ON licenses (user_id, granted_at DESC, request_date DESC);
