ALTER TABLE users
    ADD COLUMN IF NOT EXISTS deactivated_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS deactivated_by UUID NULL REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS deactivation_reason TEXT NULL,
    ADD COLUMN IF NOT EXISTS reactivated_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS reactivated_by UUID NULL REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS reactivation_reason TEXT NULL;

CREATE INDEX IF NOT EXISTS idx_users_deactivated_at ON users(deactivated_at);
CREATE INDEX IF NOT EXISTS idx_users_deactivated_by ON users(deactivated_by);
