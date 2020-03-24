CREATE TABLE download_history (
	id BIGINT PRIMARY KEY NOT NULL,
	bundle_id BIGINT NOT NULL,
	user_id BIGINT NOT NULL,
	download_time TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT download_history_users_fk FOREIGN KEY (user_id) REFERENCES users (id),
	CONSTRAINT download_history_download_bundle_fk FOREIGN KEY (bundle_id) REFERENCES download_bundle (id)
);

CREATE SEQUENCE download_history_seq;

CREATE INDEX download_history_bundle_id ON download_history (bundle_id);
CREATE INDEX download_history_user_id ON download_history (user_id);