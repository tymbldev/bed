-- This script adds cities_processed flag to the countries table
-- This flag indicates whether cities have been fetched for a country using GenAI

-- Add cities_processed column to countries table
ALTER TABLE countries 
ADD COLUMN IF NOT EXISTS cities_processed BOOLEAN DEFAULT FALSE;

-- Add index on cities_processed for better performance
CREATE INDEX IF NOT EXISTS idx_countries_cities_processed ON countries(cities_processed); 