CREATE TABLE saved_files_info (
  id INTEGER PRIMARY KEY NOT NULL,
  uuid CHARACTER VARYING NOT NULL UNIQUE,
  created TIMESTAMP WITH TIME ZONE NOT NULL,
  cdm_version CHARACTER VARYING NOT NULL,
  is_deleted BOOLEAN DEFAULT (FALSE),
  cpt4 BOOLEAN DEFAULT (FALSE)
);

CREATE SEQUENCE saved_files_info_seq;

ALTER TABLE saved_file DROP COLUMN uuid;
ALTER TABLE saved_file DROP COLUMN created;
ALTER TABLE saved_file DROP COLUMN cdm_version;
ALTER TABLE saved_file DROP COLUMN is_deleted;

ALTER TABLE saved_file ADD COLUMN saved_files_info_id INTEGER;

ALTER TABLE saved_file ADD CONSTRAINT saved_file_saved_files_info_fk FOREIGN KEY (saved_files_info_id) REFERENCES saved_files_info (id) ON UPDATE CASCADE ON DELETE CASCADE;
