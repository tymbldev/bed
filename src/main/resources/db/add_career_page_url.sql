-- Add career_page_url column to companies table
ALTER TABLE companies ADD COLUMN career_page_url TEXT AFTER website;

-- Update existing companies to have empty career_page_url
UPDATE companies SET career_page_url = '' WHERE career_page_url IS NULL; 