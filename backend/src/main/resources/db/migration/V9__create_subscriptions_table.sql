-- V9__create_subscriptions_table.sql
-- Create subscriptions table for membership management

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    auto_renew BOOLEAN NOT NULL DEFAULT true,
    payment_method VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_student_id ON subscriptions(student_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_plan_id ON subscriptions(plan_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_dates ON subscriptions(start_date, end_date);

-- Add comments
COMMENT ON TABLE subscriptions IS 'Stores subscription records for students/members';
COMMENT ON COLUMN subscriptions.student_id IS 'Reference to the student';
COMMENT ON COLUMN subscriptions.plan_id IS 'Reference to the subscribed plan';
COMMENT ON COLUMN subscriptions.start_date IS 'Subscription start date';
COMMENT ON COLUMN subscriptions.end_date IS 'Subscription end date';
COMMENT ON COLUMN subscriptions.status IS 'Subscription status: ACTIVE, EXPIRED, CANCELLED, SUSPENDED';
COMMENT ON COLUMN subscriptions.auto_renew IS 'Whether the subscription auto-renews';
COMMENT ON COLUMN subscriptions.payment_method IS 'Payment method used';
