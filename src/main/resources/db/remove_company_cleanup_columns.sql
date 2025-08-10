-- Remove company cleanup and processed name columns
-- This script removes the following columns from the companies table:
-- - processed_name
-- - processed_name_generated
-- - cleanup_processed
-- - cleanup_processed_at
-- - is_junk
-- - junk_reason
-- - parent_company_name

-- Remove columns from companies table
ALTER TABLE companies 
DROP COLUMN IF EXISTS processed_name,
DROP COLUMN IF EXISTS processed_name_generated,
DROP COLUMN IF EXISTS cleanup_processed,
DROP COLUMN IF EXISTS cleanup_processed_at,
DROP COLUMN IF EXISTS is_junk,
DROP COLUMN IF EXISTS junk_reason,
DROP COLUMN IF EXISTS parent_company_name;

-- Drop related indexes
DROP INDEX IF EXISTS idx_companies_processed_name_generated;
DROP INDEX IF EXISTS idx_companies_processed_name_unique;

-- Note: Keep the junk_identified column as it's still used for other purposes
-- The junk_identified column is separate from the removed is_junk column
