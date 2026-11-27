ALTER TABLE users_data
    ADD COLUMN reset_delivery_id uuid,
    ADD COLUMN reset_delivery_due_at timestamptz,
    ADD CHECK ((reset_delivery_id IS NULL) = (reset_delivery_due_at IS NULL));

CREATE INDEX users_data_pending_reset_email ON users_data (reset_delivery_due_at, athena_user_id)
    WHERE reset_delivery_due_at IS NOT NULL;
