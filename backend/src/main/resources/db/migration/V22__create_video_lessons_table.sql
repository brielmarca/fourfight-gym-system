CREATE TABLE video_lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    modality VARCHAR(30) NOT NULL,
    video_url TEXT NOT NULL,
    embed_url TEXT,
    provider VARCHAR(20) NOT NULL,
    minimum_plan_rank INTEGER NOT NULL,
    professor_id UUID REFERENCES users(id),
    created_by UUID NOT NULL REFERENCES users(id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_video_lessons_min_plan_rank CHECK (minimum_plan_rank BETWEEN 1 AND 3)
);

CREATE INDEX idx_video_lessons_active_rank ON video_lessons(active, minimum_plan_rank);
CREATE INDEX idx_video_lessons_created_by ON video_lessons(created_by);
CREATE INDEX idx_video_lessons_professor_id ON video_lessons(professor_id);
CREATE INDEX idx_video_lessons_modality ON video_lessons(modality);
