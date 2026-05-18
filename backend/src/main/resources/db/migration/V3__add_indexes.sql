-- V3__add_indexes.sql
-- All indexes for base schema tables

-- Users
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Refresh tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_deleted_at ON refresh_tokens(deleted_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_revoked ON refresh_tokens(user_id, revoked) WHERE revoked = false;

-- Plans
CREATE INDEX IF NOT EXISTS idx_plans_is_active ON plans(is_active);
CREATE INDEX IF NOT EXISTS idx_plans_deleted_at ON plans(deleted_at);

-- Memberships
CREATE INDEX IF NOT EXISTS idx_memberships_user_id ON memberships(user_id);
CREATE INDEX IF NOT EXISTS idx_memberships_plan_id ON memberships(plan_id);
CREATE INDEX IF NOT EXISTS idx_memberships_status ON memberships(status);
CREATE INDEX IF NOT EXISTS idx_memberships_deleted_at ON memberships(deleted_at);
CREATE INDEX IF NOT EXISTS idx_memberships_user_status ON memberships(user_id, status) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_memberships_end_date ON memberships(end_date);

-- Trainers
CREATE INDEX IF NOT EXISTS idx_trainers_user_id ON trainers(user_id);
CREATE INDEX IF NOT EXISTS idx_trainers_is_active ON trainers(is_active);
CREATE INDEX IF NOT EXISTS idx_trainers_deleted_at ON trainers(deleted_at);

-- Classes
CREATE INDEX IF NOT EXISTS idx_classes_trainer_id ON classes(trainer_id);
CREATE INDEX IF NOT EXISTS idx_classes_schedule ON classes(schedule);
CREATE INDEX IF NOT EXISTS idx_classes_status ON classes(status);
CREATE INDEX IF NOT EXISTS idx_classes_deleted_at ON classes(deleted_at);
CREATE INDEX IF NOT EXISTS idx_classes_schedule_status ON classes(schedule, status);
CREATE INDEX IF NOT EXISTS idx_classes_trainer_schedule ON classes(trainer_id, schedule);

-- Class enrollments
CREATE INDEX IF NOT EXISTS idx_class_enrollments_class_id ON class_enrollments(class_id);
CREATE INDEX IF NOT EXISTS idx_class_enrollments_user_id ON class_enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_class_enrollments_deleted_at ON class_enrollments(deleted_at);
CREATE INDEX IF NOT EXISTS idx_class_enrollments_class_attended ON class_enrollments(class_id, attended) WHERE attended = true;

-- Schedule requests
CREATE INDEX IF NOT EXISTS idx_schedule_requests_user_id ON schedule_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_schedule_requests_trainer_id ON schedule_requests(trainer_id);
CREATE INDEX IF NOT EXISTS idx_schedule_requests_status ON schedule_requests(status);
CREATE INDEX IF NOT EXISTS idx_schedule_requests_deleted_at ON schedule_requests(deleted_at);
CREATE INDEX IF NOT EXISTS idx_schedule_requests_trainer_status ON schedule_requests(trainer_id, status) WHERE status = 'PENDING';

-- Payments
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_membership_id ON payments(membership_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_deleted_at ON payments(deleted_at);
CREATE INDEX IF NOT EXISTS idx_payments_status_created ON payments(status, created_at);
CREATE INDEX IF NOT EXISTS idx_payments_user_status ON payments(user_id, status);

-- Notifications
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read_at ON notifications(read_at);
CREATE INDEX IF NOT EXISTS idx_notifications_deleted_at ON notifications(deleted_at);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, read_at) WHERE read_at IS NULL;

-- Contacts
CREATE INDEX IF NOT EXISTS idx_contacts_status ON contacts(status);
CREATE INDEX IF NOT EXISTS idx_contacts_deleted_at ON contacts(deleted_at);

-- Audit logs
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_id ON audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action_created ON audit_logs(action, created_at);

-- Rate limit buckets
CREATE INDEX IF NOT EXISTS idx_rate_limit_buckets_key ON rate_limit_buckets(key);
