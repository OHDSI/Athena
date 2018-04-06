UPDATE download_bundle SET status = 'DELETED' where is_deleted = true;
ALTER TABLE download_bundle DROP COLUMN is_deleted;