-- Add rank column to industries table
ALTER TABLE industries ADD COLUMN rank INT DEFAULT 0;

-- Update existing industries with rank based on their current order
-- This will be used for sorting in the getIndustryWiseCompanies endpoint
UPDATE industries SET rank = id WHERE rank IS NULL OR rank = 0;

-- Create index on rank for better performance
CREATE INDEX idx_industries_rank ON industries(rank);

-- Verify the changes
SELECT id, name, rank FROM industries ORDER BY rank;
