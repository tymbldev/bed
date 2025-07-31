-- Add shortname_generated column to companies table
-- This script adds a column to track whether a shortname has been generated for each company

-- Add shortname_generated column
ALTER TABLE companies ADD COLUMN IF NOT EXISTS shortname_generated BOOLEAN DEFAULT FALSE;

-- Create index for better performance when querying unprocessed companies
CREATE INDEX IF NOT EXISTS idx_companies_shortname_generated ON companies(shortname_generated);

-- Update existing companies to have shortname_generated = false
UPDATE companies SET shortname_generated = FALSE WHERE shortname_generated IS NULL;

-- Add comment for documentation
COMMENT ON COLUMN companies.shortname_generated IS 'Flag to track if shortname has been generated for the company'; 