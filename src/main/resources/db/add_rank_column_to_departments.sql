-- Add rank column to departments table
-- This migration adds a rank field to the departments table with a default value of 0
-- Records will be sorted by rank in ascending order

-- Add rank column with default value 0
ALTER TABLE departments ADD COLUMN IF NOT EXISTS rank INT DEFAULT 0;

-- Add index for better performance on rank-based queries
CREATE INDEX IF NOT EXISTS idx_departments_rank ON departments(rank);

-- Update existing records to have rank 0 if they are NULL
UPDATE departments SET rank = 0 WHERE rank IS NULL;
