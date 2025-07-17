-- Migration script to move large text columns to separate company_content table
-- This addresses MariaDB limitations with multiple LONGTEXT columns in a single table

-- Step 1: Create the new company_content table
CREATE TABLE IF NOT EXISTS company_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    about_us_original LONGTEXT,
    culture_original LONGTEXT,
    content_shortened BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_company_id (company_id),
    INDEX idx_content_shortened (content_shortened)
);

-- Step 2: Migrate existing data from companies table to company_content table
INSERT INTO company_content (company_id, about_us_original, culture_original, content_shortened, created_at, updated_at)
SELECT 
    id as company_id,
    about_us_original,
    culture_original,
    content_shortened,
    created_at,
    updated_at
FROM companies 
WHERE about_us_original IS NOT NULL 
   OR culture_original IS NOT NULL 
   OR content_shortened = TRUE;

-- Step 3: Remove the columns from the companies table
ALTER TABLE companies 
DROP COLUMN IF EXISTS about_us_original,
DROP COLUMN IF EXISTS culture_original,
DROP COLUMN IF EXISTS content_shortened;

-- Step 4: Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_company_content_company_id ON company_content(company_id);
CREATE INDEX IF NOT EXISTS idx_company_content_shortened ON company_content(content_shortened); 