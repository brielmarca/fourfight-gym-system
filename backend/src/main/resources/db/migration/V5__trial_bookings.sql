-- V5__trial_bookings.sql
CREATE TABLE trial_bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    program VARCHAR(50) NOT NULL DEFAULT 'JIU-JITSU',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by UUID REFERENCES users(id),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_trial_bookings_status ON trial_bookings(status);
CREATE INDEX idx_trial_bookings_email ON trial_bookings(email);
CREATE INDEX idx_trial_bookings_program ON trial_bookings(program);