-- V8__plan_optional_fields.sql
-- Add optional fields to plans table for enhanced frontend display

-- Add level column (e.g., "Beginner", "Intermediate", "Advanced")
ALTER TABLE plans ADD COLUMN IF NOT EXISTS level VARCHAR(50);

-- Add instructor column
ALTER TABLE plans ADD COLUMN IF NOT EXISTS instructor VARCHAR(255);

-- Add schedule column as JSONB to store schedule information
ALTER TABLE plans ADD COLUMN IF NOT EXISTS schedule JSONB;

-- Set default empty array for features where it's NULL
UPDATE plans SET features = '[]'::jsonb WHERE features IS NULL;

-- Update existing plans with sample data for better frontend display
UPDATE plans SET
    level = CASE
        WHEN name = 'Basic' THEN 'Beginner'
        WHEN name = 'Standard' THEN 'Intermediate'
        WHEN name = 'Premium' THEN 'Advanced'
        ELSE level
    END,
    instructor = CASE
        WHEN name = 'Basic' THEN 'Instrutor Geral'
        WHEN name = 'Standard' THEN 'Instrutor Especializado'
        WHEN name = 'Premium' THEN 'Mestre de Jiu-Jitsu'
        ELSE instructor
    END,
    schedule = CASE
        WHEN name = 'Basic' THEN '["Seg/Qua/Sex: 06h-10h", "Ter/Qui: 18h-22h"]'::jsonb
        WHEN name = 'Standard' THEN '["Seg-Sex: 06h-22h", "Sab: 08h-14h"]'::jsonb
        WHEN name = 'Premium' THEN '["Seg-Dom: 24h", "Aulas especiais aos sabados"]'::jsonb
        ELSE schedule
    END
WHERE is_active = true AND (level IS NULL OR instructor IS NULL OR schedule IS NULL);
