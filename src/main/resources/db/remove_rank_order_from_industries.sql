-- Remove rank_order column from industries table
-- This script removes the rank_order column and any related dependencies

-- Drop the rank_order column from industries table
ALTER TABLE industries DROP COLUMN IF EXISTS rank_order;

-- Drop any indexes related to rank_order if they exist
DROP INDEX IF EXISTS idx_industries_rank_order;

-- Add comment to document the change
COMMENT ON TABLE industries IS 'Industries table - rank_order column removed, sorting now based on company count and "other" logic';

