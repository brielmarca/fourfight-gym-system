CREATE TABLE IF NOT EXISTS pre_registration_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    age INTEGER NOT NULL,
    phone VARCHAR(20) NOT NULL,
    parish_or_area VARCHAR(255) NOT NULL,
    has_martial_arts_experience BOOLEAN NOT NULL,
    martial_arts_experience_details TEXT,
    training_goal TEXT NOT NULL,
    preferred_modality VARCHAR(40) NOT NULL,
    preferred_modality_other VARCHAR(255),
    preferred_training_time VARCHAR(40) NOT NULL,
    preferred_training_time_other VARCHAR(255),
    values_martial_arts_philosophy BOOLEAN NOT NULL,
    preferred_contact_method VARCHAR(20) NOT NULL,
    preferred_contact_method_other VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pre_registration_profile_days (
    profile_id UUID NOT NULL REFERENCES pre_registration_profiles(id) ON DELETE CASCADE,
    day VARCHAR(20) NOT NULL,
    PRIMARY KEY (profile_id, day)
);

CREATE INDEX IF NOT EXISTS idx_pre_registration_profiles_user_id ON pre_registration_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_pre_registration_profiles_created_at ON pre_registration_profiles(created_at DESC);
