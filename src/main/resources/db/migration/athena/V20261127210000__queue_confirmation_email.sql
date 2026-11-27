ALTER TABLE users_data
    ADD COLUMN confirmation_delivery_id uuid,
    ADD COLUMN confirmation_delivery_due_at timestamptz,
    ADD CHECK ((confirmation_delivery_id IS NULL) = (confirmation_delivery_due_at IS NULL));

CREATE INDEX users_data_pending_confirmation_email ON users_data (confirmation_delivery_due_at, athena_user_id)
    WHERE confirmation_delivery_due_at IS NOT NULL;
