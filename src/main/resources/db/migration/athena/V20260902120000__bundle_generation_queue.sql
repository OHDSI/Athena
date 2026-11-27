ALTER TABLE download_bundle
    ADD COLUMN generation_started_at TIMESTAMP,
    ADD COLUMN generation_heartbeat_at TIMESTAMP,
    ADD COLUMN generation_worker VARCHAR(255),
    ADD COLUMN generation_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN generation_failure VARCHAR(1000);

CREATE INDEX download_bundle_generation_queue_idx
    ON download_bundle (created, id)
    WHERE status = 'PENDING';
