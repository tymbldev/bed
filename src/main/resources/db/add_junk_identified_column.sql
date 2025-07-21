-- Add junk_identified column to companies table
-- This migration adds a field to track companies that have been identified as junk/invalid names

ALTER TABLE companies 
ADD COLUMN IF NOT EXISTS junk_identified BOOLEAN DEFAULT FALSE;

-- Create index for efficient querying of junk companies
CREATE INDEX IF NOT EXISTS idx_companies_junk_identified ON companies(junk_identified); 