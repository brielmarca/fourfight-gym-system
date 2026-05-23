CREATE TABLE IF NOT EXISTS pre_registration_leads (
    id UUID PRIMARY KEY,
    submitted_at TIMESTAMP NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    age INTEGER,
    phone VARCHAR(50) NOT NULL,
    parish VARCHAR(255),
    has_martial_arts_experience BOOLEAN,
    martial_arts_experience_details TEXT,
    training_goal TEXT,
    preferred_modalities TEXT,
    preferred_training_times TEXT,
    preferred_training_days TEXT,
    philosophy_important BOOLEAN,
    preferred_contact_method VARCHAR(100),
    source VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pre_registration_leads_dedupe UNIQUE (full_name, phone, submitted_at)
);

CREATE INDEX IF NOT EXISTS idx_pre_registration_leads_submitted_at
    ON pre_registration_leads (submitted_at DESC);

CREATE INDEX IF NOT EXISTS idx_pre_registration_leads_status
    ON pre_registration_leads (status);
