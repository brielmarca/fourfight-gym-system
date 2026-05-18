-- V2__seed_base_data.sql
-- Seed roles, default plans, and admin user

-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
    ('ADMIN', 'Administrator', 'Full system access'),
    ('MANAGER', 'Manager', 'Manage staff and operations'),
    ('TRAINER', 'Trainer', 'Train clients and manage classes'),
    ('CLIENT', 'Client', 'Gym member with membership')
ON CONFLICT (id) DO NOTHING;

-- Insert default plans
INSERT INTO plans (id, name, description, price, duration_days, max_classes) VALUES
    (uuid_generate_v4(), 'Basic', 'Basic membership with limited access', 29.99, 30, 8),
    (uuid_generate_v4(), 'Standard', 'Standard membership with full gym access', 49.99, 30, 20),
    (uuid_generate_v4(), 'Premium', 'Premium membership with unlimited classes', 89.99, 30, 100),
    (uuid_generate_v4(), 'Annual Basic', 'Annual basic membership', 299.99, 365, 96),
    (uuid_generate_v4(), 'Annual Premium', 'Annual premium membership', 799.99, 365, 1000)
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
