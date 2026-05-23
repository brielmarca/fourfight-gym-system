CREATE TABLE class_schedule (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    modality VARCHAR(30) NOT NULL,
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    instructor_name VARCHAR(255) NOT NULL,
    level VARCHAR(20) NOT NULL,
    location VARCHAR(255),
    capacity INTEGER,
    active BOOLEAN NOT NULL DEFAULT true,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_class_schedule_modality CHECK (
        modality IN ('JIU_JITSU', 'BOXE_KICKBOXING', 'CAPOEIRA', 'MMA')
    ),
    CONSTRAINT chk_class_schedule_day_of_week CHECK (
        day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')
    ),
    CONSTRAINT chk_class_schedule_level CHECK (
        level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ALL_LEVELS')
    ),
    CONSTRAINT chk_class_schedule_time_range CHECK (start_time < end_time),
    CONSTRAINT chk_class_schedule_capacity CHECK (capacity IS NULL OR capacity > 0)
);

CREATE INDEX idx_class_schedule_active ON class_schedule(active);
CREATE INDEX idx_class_schedule_day_time ON class_schedule(day_of_week, start_time);
