-- V10__fix_refresh_token_hash_length.sql
-- Increase token_hash column size to accommodate longer hashes

ALTER TABLE refresh_tokens ALTER COLUMN token_hash TYPE VARCHAR(512);

COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256 hash of the refresh token (increased from 255 to 512)';
