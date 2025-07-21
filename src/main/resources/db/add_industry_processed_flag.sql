-- This script adds industry_processed flag to the companies table
-- This flag indicates whether industry detection has been processed for a company

-- Add industry_processed column to companies table
ALTER TABLE companies 
ADD COLUMN IF NOT EXISTS industry_processed BOOLEAN DEFAULT FALSE;

-- Add index on industry_processed for better performance
CREATE INDEX IF NOT EXISTS idx_companies_industry_processed ON companies(industry_processed); 