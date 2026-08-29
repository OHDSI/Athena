-- Every row created before the token column existed carries the empty-string default, and the
-- token is what makes an accept-by-mail link specific to one licence.
--
-- gen_random_uuid() rather than md5(random()::text): random() is a fast non-cryptographic PRNG
-- seeded per session, so its output is predictable from other values drawn in the same session.
-- gen_random_uuid() is backed by the server's strong RNG and is core PostgreSQL since 13, so no
-- pgcrypto extension is needed. This also matches the application, which generates new tokens
-- with UUID.randomUUID() -- see License.java.
UPDATE licenses
SET token = replace(gen_random_uuid()::text, '-', '')
WHERE token IS NULL OR btrim(token, E' \t\n\r\f\v') = '';

-- The column has been NOT NULL since it was added, so dropping the default makes an INSERT that
-- omits the token fail outright instead of quietly storing ''.
ALTER TABLE licenses ALTER COLUMN token DROP DEFAULT;

-- NOT NULL is restated rather than assumed. A CHECK is not a null check: the trimmed comparison
-- evaluates to NULL for a NULL token, and a CHECK constraint accepts anything that is not false.
-- The two constraints together are what make "present and non-blank" true.
--
-- Trimmed rather than a bare `<> ''`: a token of whitespace is as useless as an empty one, since
-- LicenseServiceImpl.checkLicense rejects a blank token via StringUtils.isBlank before the row is
-- ever looked up -- such a licence could never be accepted by mail. The character set is spelled
-- out because one-argument btrim() strips spaces only, which would let a tab through.
ALTER TABLE licenses ALTER COLUMN token SET NOT NULL;
ALTER TABLE licenses ADD CONSTRAINT licenses_token_not_blank
    CHECK (btrim(token, E' \t\n\r\f\v') <> '');
