CREATE TABLE licenses (
  id BIGINT PRIMARY KEY NOT NULL,
  vocabulary_id_v4 BIGINT NOT NULL,
  user_id BIGINT NOT NULL, --
  CONSTRAINT users_licenses_pk UNIQUE (vocabulary_id_v4, user_id),
  CONSTRAINT licenses_vocabulary_conversion_fk FOREIGN KEY (vocabulary_id_v4) REFERENCES vocabulary_conversion (vocabulary_id_v4),
  CONSTRAINT users_license_id_license_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE SEQUENCE licenses_seq;
