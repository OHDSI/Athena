CREATE TABLE VOCABULARY_CONVERSION
(	 VOCABULARY_ID_V4 BIGINT,
   VOCABULARY_ID_V5 VARCHAR(20),
   OMOP_REQ VARCHAR(1),
   CLICK_DEFAULT VARCHAR(1),
   AVAILABLE VARCHAR(25),
   URL VARCHAR(256),
   CLICK_DISABLED VARCHAR(1),
   LATEST_UPDATE DATE
);

CREATE TABLE download_history (
  id INTEGER PRIMARY KEY NOT NULL,
  saved_files_info_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL
);

ALTER TABLE download_history ADD CONSTRAINT download_history_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE download_history ADD CONSTRAINT download_history_saved_files_info_id_fk FOREIGN KEY (saved_files_info_id) REFERENCES saved_files_info (id) ON DELETE CASCADE;

CREATE SEQUENCE download_history_seq;

CREATE TABLE download_history_item (
  id INTEGER PRIMARY KEY NOT NULL,
  download_history_id INTEGER NOT NULL,
  vocabulary_id_v4 INTEGER NOT NULL
);

CREATE SEQUENCE download_history_item_seq;

ALTER TABLE download_history_item ADD CONSTRAINT download_history_item_download_history_id_fk FOREIGN KEY (download_history_id) REFERENCES download_history (id) ON DELETE CASCADE;

ALTER TABLE VOCABULARY_CONVERSION ADD CONSTRAINT vocabulary_id_v4_unique UNIQUE (VOCABULARY_ID_V4);
ALTER TABLE download_history_item ADD CONSTRAINT download_history_item_vocabulary_id_v4_fk FOREIGN KEY (vocabulary_id_v4) REFERENCES VOCABULARY_CONVERSION (VOCABULARY_ID_V4) ON DELETE CASCADE;