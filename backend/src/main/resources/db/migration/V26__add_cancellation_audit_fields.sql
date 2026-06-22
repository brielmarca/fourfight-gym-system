-- V26__add_cancellation_audit_fields.sql
-- Add cancellation audit tracking to memberships

ALTER TABLE memberships ADD COLUMN IF NOT EXISTS cancellation_requested_at TIMESTAMP;
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS cancellation_reason VARCHAR(500);
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS cancellation_source VARCHAR(30);
