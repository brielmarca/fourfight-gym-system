-- V2__indexes.sql
-- Additional indexes for performance

-- Users
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'users') THEN
    CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);
    CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
  END IF;
END $$;

-- Memberships
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'memberships') THEN
    CREATE INDEX IF NOT EXISTS idx_memberships_user_status ON memberships(user_id, status) WHERE status = 'ACTIVE';
    CREATE INDEX IF NOT EXISTS idx_memberships_end_date ON memberships(end_date);
  END IF;
END $$;

-- Classes
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'classes') THEN
    CREATE INDEX IF NOT EXISTS idx_classes_schedule_status ON classes(schedule, status);
    CREATE INDEX IF NOT EXISTS idx_classes_trainer_schedule ON classes(trainer_id, schedule);
  END IF;
END $$;

-- Class Enrollments
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'class_enrollments') THEN
    CREATE INDEX IF NOT EXISTS idx_class_enrollments_class_attended ON class_enrollments(class_id, attended) WHERE attended = true;
  END IF;
END $$;

-- Payments
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'payments') THEN
    CREATE INDEX IF NOT EXISTS idx_payments_status_created ON payments(status, created_at);
    CREATE INDEX IF NOT EXISTS idx_payments_user_status ON payments(user_id, status);
  END IF;
END $$;

-- Notifications
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'notifications') THEN
    CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, read_at) WHERE read_at IS NULL;
  END IF;
END $$;

-- Audit Logs
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'audit_logs') THEN
    CREATE INDEX IF NOT EXISTS idx_audit_logs_action_created ON audit_logs(action, created_at);
  END IF;
END $$;

-- Schedule Requests
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'schedule_requests') THEN
    CREATE INDEX IF NOT EXISTS idx_schedule_requests_trainer_status ON schedule_requests(trainer_id, status) WHERE status = 'PENDING';
  END IF;
END $$;

-- Refresh Tokens
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'refresh_tokens') THEN
    CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_revoked ON refresh_tokens(user_id, revoked) WHERE revoked = false;
  END IF;
END $$;
