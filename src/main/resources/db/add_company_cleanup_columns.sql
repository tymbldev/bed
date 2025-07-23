-- Add cleanup-related columns to companies table
-- This script adds columns to track company cleanup processing

-- Add shortname column if it doesn't exist
ALTER TABLE companies ADD COLUMN IF NOT EXISTS shortname VARCHAR(255);

-- Add cleanup_processed column
ALTER TABLE companies ADD COLUMN IF NOT EXISTS cleanup_processed BOOLEAN DEFAULT FALSE;

-- Add cleanup_processed_at column
ALTER TABLE companies ADD COLUMN IF NOT EXISTS cleanup_processed_at TIMESTAMP;

-- Add is_junk column for flagging junk companies
ALTER TABLE companies ADD COLUMN IF NOT EXISTS is_junk BOOLEAN DEFAULT FALSE;

-- Add junk_reason column to store reason for junk marking
ALTER TABLE companies ADD COLUMN IF NOT EXISTS junk_reason TEXT;

-- Add parent_company_name column to store parent company name
ALTER TABLE companies ADD COLUMN IF NOT EXISTS parent_company_name VARCHAR(255);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_companies_cleanup_processed ON companies(cleanup_processed);
CREATE INDEX IF NOT EXISTS idx_companies_is_junk ON companies(is_junk);

-- Update existing companies to have cleanup_processed = false
UPDATE companies SET cleanup_processed = FALSE WHERE cleanup_processed IS NULL;

-- Update existing companies to have is_junk = false
UPDATE companies SET is_junk = FALSE WHERE is_junk IS NULL;

-- Add comments for documentation
COMMENT ON COLUMN companies.shortname IS 'Short name/alias for the company (e.g., Zomato for Eternal)';
COMMENT ON COLUMN companies.cleanup_processed IS 'Flag to track if company has been processed for cleanup';
COMMENT ON COLUMN companies.cleanup_processed_at IS 'Timestamp when company was processed for cleanup';
COMMENT ON COLUMN companies.is_junk IS 'Flag to mark company as junk/product entry for manual review';
COMMENT ON COLUMN companies.junk_reason IS 'Reason why company was marked as junk (e.g., Product of Amazon)';
COMMENT ON COLUMN companies.parent_company_name IS 'Name of the parent company if this is a product/service';

-- Example data for testing
-- INSERT INTO companies (name, shortname, cleanup_processed, cleanup_processed_at, is_junk, junk_reason, parent_company_name) VALUES 
-- ('AWS', NULL, FALSE, NULL, FALSE, NULL, NULL),
-- ('Google Cloud', NULL, FALSE, NULL, FALSE, NULL, NULL),
-- ('Microsoft Azure', NULL, FALSE, NULL, FALSE, NULL, NULL),
-- ('Netflix', NULL, FALSE, NULL, FALSE, NULL, NULL);

SELECT 'Company cleanup columns added successfully' as status; 