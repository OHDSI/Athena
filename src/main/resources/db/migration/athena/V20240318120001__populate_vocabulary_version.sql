UPDATE download_bundle
SET vocabulary_version = CAST(TO_CHAR(TO_DATE(RIGHT(release_version, 9), 'DD-MON-YY'), 'YYYYMMDD') AS INTEGER)
WHERE vocabulary_version IS NULL;