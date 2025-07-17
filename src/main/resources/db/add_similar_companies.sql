-- Add similar companies columns to companies table
-- This migration adds fields to store similar companies and track processing status

ALTER TABLE companies 
ADD COLUMN IF NOT EXISTS similar_companies_by_name TEXT,
ADD COLUMN IF NOT EXISTS similar_companies_by_id TEXT,
ADD COLUMN IF NOT EXISTS similar_companies_processed BOOLEAN DEFAULT FALSE;

-- Add index for better performance when querying unprocessed companies
CREATE INDEX IF NOT EXISTS idx_companies_similar_processed ON companies(similar_companies_processed);

-- Add index for better performance when querying by primary industry
CREATE INDEX IF NOT EXISTS idx_companies_primary_industry ON companies(primary_industry_id); 