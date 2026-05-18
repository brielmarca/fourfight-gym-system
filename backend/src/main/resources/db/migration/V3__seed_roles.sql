-- V3__seed_roles.sql
-- Seed roles and default data

-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
    (gen_random_uuid(), 'ADMIN', 'System administrator with full access'),
    (gen_random_uuid(), 'MANAGER', 'Gym manager with operational access'),
    (gen_random_uuid(), 'TRAINER', 'Fitness trainer with client management'),
    (gen_random_uuid(), 'CLIENT', 'Regular gym member')
ON CONFLICT (name) DO NOTHING;

-- Insert default plans
INSERT INTO plans (id, name, description, price, duration_days, max_classes) VALUES
    (gen_random_uuid(), 'Basic', 'Basic membership with limited access', 29.99, 30, 8),
    (gen_random_uuid(), 'Standard', 'Standard membership with full gym access', 49.99, 30, 20),
    (gen_random_uuid(), 'Premium', 'Premium membership with unlimited classes', 89.99, 30, 100),
    (gen_random_uuid(), 'Annual Basic', 'Annual basic membership', 299.99, 365, 96),
    (gen_random_uuid(), 'Annual Premium', 'Annual premium membership', 799.99, 365, 1000)
ON CONFLICT DO NOTHING;

-- Insert sample admin user (password: admin123 - BCrypt hash)
INSERT INTO users (id, name, email, password_hash, role, is_active)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'System Admin',
    'admin@gym.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYqL1Y6JfzW',
    'ADMIN',
    true
)
ON CONFLICT (email) DO NOTHING;