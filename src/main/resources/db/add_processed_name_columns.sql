-- Add processed name columns to entities for deduplication
-- This migration adds fields to store processed names and track processing status

-- Add processed name columns to countries table
ALTER TABLE countries 
ADD COLUMN IF NOT EXISTS processed_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS processed_name_generated BOOLEAN DEFAULT FALSE;

-- Add processed name columns to cities table
ALTER TABLE cities 
ADD COLUMN IF NOT EXISTS processed_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS processed_name_generated BOOLEAN DEFAULT FALSE;

-- Add processed name columns to companies table
ALTER TABLE companies 
ADD COLUMN IF NOT EXISTS processed_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS processed_name_generated BOOLEAN DEFAULT FALSE;

-- Add processed name columns to designations table
ALTER TABLE designations 
ADD COLUMN IF NOT EXISTS processed_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS processed_name_generated BOOLEAN DEFAULT FALSE;

-- Add indexes for better performance when querying unprocessed entities
CREATE INDEX IF NOT EXISTS idx_countries_processed_name_generated ON countries(processed_name_generated);
CREATE INDEX IF NOT EXISTS idx_cities_processed_name_generated ON cities(processed_name_generated);
CREATE INDEX IF NOT EXISTS idx_companies_processed_name_generated ON companies(processed_name_generated);
CREATE INDEX IF NOT EXISTS idx_designations_processed_name_generated ON designations(processed_name_generated);

-- Add unique indexes on processed_name to ensure uniqueness
CREATE UNIQUE INDEX IF NOT EXISTS idx_countries_processed_name_unique ON countries(processed_name) WHERE processed_name IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_cities_processed_name_unique ON cities(processed_name) WHERE processed_name IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_companies_processed_name_unique ON companies(processed_name) WHERE processed_name IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_designations_processed_name_unique ON designations(processed_name) WHERE processed_name IS NOT NULL; 