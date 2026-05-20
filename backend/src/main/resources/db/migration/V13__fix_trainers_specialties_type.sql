-- V13__fix_trainers_specialties_type.sql
-- Change specialties from TEXT[] to TEXT to match Trainer entity

ALTER TABLE trainers ALTER COLUMN specialties TYPE TEXT;
