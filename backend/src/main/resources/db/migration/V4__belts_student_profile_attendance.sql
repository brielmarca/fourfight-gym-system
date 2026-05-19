-- V4__belts_student_profile_attendance.sql
-- Belt ranking system, student profiles, and training attendance

-- Belts table
CREATE TABLE belts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    color_hex VARCHAR(7),
    rank_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_belts_rank_order ON belts(rank_order);
CREATE INDEX IF NOT EXISTS idx_belts_is_active ON belts(is_active);

-- Default belts (Karate/Grappling ranking)
INSERT INTO belts (name, color_hex, rank_order) VALUES
('Branca', '#FFFFFF', 1),
('Cinza', '#808080', 2),
('Amarela', '#FFD700', 3),
('Laranja', '#FFA500', 4),
('Verde', '#008000', 5),
('Azul', '#0000FF', 6),
('Roxa', '#800080', 7),
('Marrom', '#8B4513', 8),
('Preta', '#000000', 9)
ON CONFLICT DO NOTHING;

-- Student profiles table
CREATE TABLE student_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    belt_id UUID REFERENCES belts(id),
    training_days VARCHAR(20),
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(20),
    medical_notes TEXT,
    recovery_notes TEXT,
    goals TEXT,
    observations TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_student_profiles_user_id ON student_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_student_profiles_belt_id ON student_profiles(belt_id);

-- Training attendance table
CREATE TABLE training_attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    present BOOLEAN NOT NULL DEFAULT false,
    class_id UUID,
    notes TEXT,
    recorded_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE(student_id, date)
);

CREATE INDEX IF NOT EXISTS idx_training_attendance_student_id ON training_attendance(student_id);
CREATE INDEX IF NOT EXISTS idx_training_attendance_date ON training_attendance(date);

-- Attendance table (for student attendance tracking)
CREATE TABLE attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id),
    attendance_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_attendance_student_id ON attendance(student_id);
CREATE INDEX IF NOT EXISTS idx_attendance_attendance_date ON attendance(attendance_date);
