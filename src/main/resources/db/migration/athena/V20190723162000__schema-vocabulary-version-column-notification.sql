ALTER TABLE notifications
    ADD COLUMN actual_version VARCHAR(255);
ALTER TABLE notifications
    ADD COLUMN vocabulary_code VARCHAR(255);

ALTER TABLE VOCABULARY_CONVERSION
    ADD CONSTRAINT vocabulary_id_v5_unique UNIQUE (VOCABULARY_ID_V5);

ALTER TABLE notifications
    ADD CONSTRAINT notifications_vocabulary_conversion_vocabulary_code_fk FOREIGN KEY (vocabulary_code) REFERENCES vocabulary_conversion (vocabulary_id_v5);