-- Add rank_order column to industries table
ALTER TABLE industries ADD COLUMN rank_order INT DEFAULT 0;

-- Update existing industries with rank based on their current order
-- This will be used for sorting in the getIndustryWiseCompanies endpoint
UPDATE industries SET rank_order = id WHERE rank_order IS NULL OR rank_order = 0;

-- Create index on rank_order for better performance
CREATE INDEX idx_industries_rank_order ON industries(rank_order);

-- Verify the changes
SELECT id, name, rank_order FROM industries ORDER BY rank_order;
