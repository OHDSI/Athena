CREATE TABLE download_share (
	id BIGINT PRIMARY KEY NOT NULL,
	bundle_id BIGINT NOT NULL,
	user_email varchar(255) NOT NULL,
  owner_id BIGINT NOT NULL,
  owner_name varchar(255) NOT NULL
);
CREATE INDEX download_share_bundle_id ON download_share (bundle_id);
CREATE INDEX download_share_user_email ON download_share (user_email);
CREATE INDEX download_share_owner_id ON download_share (owner_id);

CREATE SEQUENCE download_share_seq;
