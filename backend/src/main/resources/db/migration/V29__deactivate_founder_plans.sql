-- Retire the four V28 Founder plans from public sale while preserving their rows
-- for historical reference. Existing memberships are intentionally untouched.
UPDATE plans
SET is_active = false,
    updated_at = CURRENT_TIMESTAMP
WHERE id IN (
    '4f280001-0000-4000-8000-000000000001',
    '4f280001-0000-4000-8000-000000000002',
    '4f280001-0000-4000-8000-000000000003',
    '4f280001-0000-4000-8000-000000000004'
)
  AND is_active = true
  AND deleted_at IS NULL;
