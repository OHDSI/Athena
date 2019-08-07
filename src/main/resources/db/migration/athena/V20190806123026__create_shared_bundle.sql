CREATE TABLE download_share (
	bundle_id int4 NOT NULL,
	user_email varchar(255) NOT NULL,
  owner_id BIGINT NOT NULL
);
CREATE INDEX download_share_bundle_id ON download_share (bundle_id);
CREATE INDEX download_share_user_email ON download_share (user_email);
