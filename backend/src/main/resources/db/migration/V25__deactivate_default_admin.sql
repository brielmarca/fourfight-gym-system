-- V25__deactivate_default_admin.sql
-- Deactivate the historical seeded default admin account.
-- This account was created by V3__seed_roles.sql with a known
-- default password and must be disabled in every environment.
-- Does NOT delete the row or set deleted_at.
-- Idempotent: safe to run if already deactivated or if the account
-- does not exist (e.g. production where it was disabled manually).

UPDATE users
SET
    is_active = false,
    deactivated_at = COALESCE(deactivated_at, NOW()),
    deactivation_reason = COALESCE(deactivation_reason, 'SECURITY: default seeded administrator account disabled via Flyway migration'),
    updated_at = NOW()
WHERE email = 'admin@gym.com'
  AND role = 'ADMIN'
  AND deleted_at IS NULL;
