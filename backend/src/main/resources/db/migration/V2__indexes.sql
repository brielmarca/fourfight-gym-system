-- V2__indexes.sql
-- Additional indexes for performance

-- Users
CREATE INDEX idx_users_name ON users(name);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Memberships - compound for active memberships query
CREATE INDEX idx_memberships_user_status ON memberships(user_id, status) WHERE status = 'ACTIVE';
CREATE INDEX idx_memberships_end_date ON memberships(end_date);

-- Classes - for finding today's classes and available slots
CREATE INDEX idx_classes_schedule_status ON classes(schedule, status);
CREATE INDEX idx_classes_trainer_schedule ON classes(trainer_id, schedule);

-- Class Enrollments - for roster queries
CREATE INDEX idx_class_enrollments_class_attended ON class_enrollments(class_id, attended) WHERE attended = true;

-- Payments - for revenue reports
CREATE INDEX idx_payments_status_created ON payments(status, created_at);
CREATE INDEX idx_payments_user_status ON payments(user_id, status);

-- Notifications - for unread count
CREATE INDEX idx_notifications_user_read ON notifications(user_id, read_at) WHERE read_at IS NULL;

-- Audit Logs - for filtering
CREATE INDEX idx_audit_logs_action_created ON audit_logs(action, created_at);

-- Schedule Requests - for trainer's pending requests
CREATE INDEX idx_schedule_requests_trainer_status ON schedule_requests(trainer_id, status) WHERE status = 'PENDING';

-- Refresh Tokens - for cleanup
CREATE INDEX idx_refresh_tokens_user_revoked ON refresh_tokens(user_id, revoked) WHERE revoked = false;