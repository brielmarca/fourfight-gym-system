CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id
    ON password_reset_tokens (user_id);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token_hash
    ON password_reset_tokens (token_hash);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at
    ON password_reset_tokens (expires_at);
