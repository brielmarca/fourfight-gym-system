-- V7__student_martial_arts.sql
-- Create martial arts, graduations, and student_martial_arts tables

-- Create martial_arts table
CREATE TABLE IF NOT EXISTS martial_arts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create graduations table
CREATE TABLE IF NOT EXISTS graduations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    level_order INTEGER NOT NULL,
    martial_art_id UUID NOT NULL REFERENCES martial_arts(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create index on graduations for martial art lookup
CREATE INDEX IF NOT EXISTS idx_graduations_martial_art_id ON graduations(martial_art_id);

-- Create student_martial_arts table
CREATE TABLE IF NOT EXISTS student_martial_arts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id),
    martial_art_id UUID NOT NULL REFERENCES martial_arts(id),
    graduation_id UUID NOT NULL REFERENCES graduations(id),
    start_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for student_martial_arts lookups
CREATE INDEX IF NOT EXISTS idx_student_martial_arts_student_id ON student_martial_arts(student_id);
CREATE INDEX IF NOT EXISTS idx_student_martial_arts_martial_art_id ON student_martial_arts(martial_art_id);
CREATE INDEX IF NOT EXISTS idx_student_martial_arts_graduation_id ON student_martial_arts(graduation_id);
