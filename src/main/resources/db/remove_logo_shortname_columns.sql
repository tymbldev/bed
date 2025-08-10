-- Remove logo_url_fetched and shortname_generated columns
-- This script removes the following columns from the companies table:
-- - logo_url_fetched
-- - shortname_generated

-- Remove columns from companies table
ALTER TABLE companies 
DROP COLUMN IF EXISTS logo_url_fetched,
DROP COLUMN IF EXISTS shortname_generated;

-- Drop related indexes
DROP INDEX IF EXISTS idx_companies_logo_url_fetched;
DROP INDEX IF EXISTS idx_companies_shortname_generated;

-- Note: The shortname column itself is preserved as it's still used for storing company shortnames
-- Only the tracking flag for whether shortname generation was completed has been removed
