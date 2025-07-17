-- Add similar designation columns to designations table
-- This migration adds fields to store similar designations and track processing status

ALTER TABLE designations 
ADD COLUMN IF NOT EXISTS similar_designations_by_name TEXT,
ADD COLUMN IF NOT EXISTS similar_designations_by_id TEXT,
ADD COLUMN IF NOT EXISTS similar_designations_processed BOOLEAN DEFAULT FALSE;

-- Add index for better performance when querying unprocessed designations
CREATE INDEX IF NOT EXISTS idx_designations_similar_processed ON designations(similar_designations_processed);

-- Add index for better performance when querying by name
CREATE INDEX IF NOT EXISTS idx_designations_name ON designations(name); 