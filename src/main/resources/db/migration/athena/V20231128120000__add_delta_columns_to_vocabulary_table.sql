ALTER TABLE download_bundle
  ADD COLUMN vocabulary_version INTEGER,
  ADD COLUMN delta BOOLEAN default false,
  ADD COLUMN delta_version INTEGER;
