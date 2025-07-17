-- Add content_shortened flag to companies table
-- This migration adds a flag to track which companies have had their content shortened

ALTER TABLE companies 
ADD COLUMN IF NOT EXISTS content_shortened BOOLEAN DEFAULT FALSE;

-- Add index for better performance when querying unprocessed companies
CREATE INDEX IF NOT EXISTS idx_companies_content_shortened ON companies(content_shortened); 