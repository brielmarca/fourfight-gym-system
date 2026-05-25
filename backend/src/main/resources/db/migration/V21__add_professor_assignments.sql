-- Add PROFESSOR role and professor assignment tables

INSERT INTO roles (id, name, description)
VALUES (gen_random_uuid(), 'PROFESSOR', 'Professor responsible for student modalities')
ON CONFLICT (name) DO NOTHING;

CREATE TABLE professor_modalities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    professor_id UUID NOT NULL REFERENCES users(id),
    modality VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_professor_modalities_professor_modality UNIQUE (professor_id, modality)
);

CREATE INDEX idx_professor_modalities_professor_id ON professor_modalities(professor_id);

CREATE TABLE professor_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    professor_id UUID NOT NULL REFERENCES users(id),
    student_id UUID NOT NULL REFERENCES users(id),
    modality VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    notes TEXT,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID REFERENCES users(id),
    updated_at TIMESTAMP
);

CREATE INDEX idx_professor_assignments_professor_id ON professor_assignments(professor_id);
CREATE INDEX idx_professor_assignments_student_id ON professor_assignments(student_id);
CREATE INDEX idx_professor_assignments_active ON professor_assignments(active);

CREATE UNIQUE INDEX uk_professor_assignment_student_modality_active
    ON professor_assignments(student_id, modality)
    WHERE active = true;
