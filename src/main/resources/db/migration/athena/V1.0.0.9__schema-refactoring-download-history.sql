ALTER TABLE saved_files_info ADD COLUMN user_id INTEGER NOT NULL;

DROP TABLE download_history CASCADE;

ALTER TABLE download_history_item  RENAME TO download_item;
ALTER TABLE saved_files_info  RENAME TO download_bundle;

ALTER SEQUENCE saved_files_info_seq RENAME TO download_bundle_seq;
ALTER TABLE saved_file RENAME COLUMN saved_files_info_id TO download_bundle_id;
