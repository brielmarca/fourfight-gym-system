-- V12__add_deleted_at_to_stripe_webhook_events.sql
-- Add missing deleted_at column for soft delete support

ALTER TABLE stripe_webhook_events ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
