ALTER TABLE vocabulary_release_version
    ADD COLUMN import_datetime TIMESTAMP,
    ADD COLUMN cached_datetime TIMESTAMP;