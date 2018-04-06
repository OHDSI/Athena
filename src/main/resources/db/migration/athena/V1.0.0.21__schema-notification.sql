CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  user_id BIGINT NOT NULL,
  vocabulary_id_v4 BIGINT NOT NULL,
  CONSTRAINT notifications_unique UNIQUE (vocabulary_id_v4, user_id),
  CONSTRAINT notifications_vocabulary_conversion_fk FOREIGN KEY (vocabulary_id_v4) REFERENCES vocabulary_conversion (vocabulary_id_v4),
  CONSTRAINT notifications_user_id_license_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);