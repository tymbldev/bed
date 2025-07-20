-- Migration script to update user_education table to use start_year and end_year
-- This script changes the schema to match frontend requirements

-- Step 1: Add new columns
ALTER TABLE user_education 
ADD COLUMN start_year INT,
ADD COLUMN end_year INT;

-- Step 2: Update existing data (if any) - convert dates to years
-- This assumes existing data has valid dates
UPDATE user_education 
SET start_year = YEAR(start_date), 
    end_year = YEAR(end_date) 
WHERE start_date IS NOT NULL AND end_date IS NOT NULL;

-- Step 3: Drop old columns
ALTER TABLE user_education 
DROP COLUMN start_date,
DROP COLUMN end_date;

-- Step 4: Make the new columns NOT NULL (optional, depending on business requirements)
-- ALTER TABLE user_education 
-- MODIFY COLUMN start_year INT NOT NULL,
-- MODIFY COLUMN end_year INT NOT NULL; 