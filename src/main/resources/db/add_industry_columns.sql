-- This script adds primary_industry_id and secondary_industries columns to the companies table

-- Add primary_industry_id column (foreign key to industries table)
ALTER TABLE companies 
ADD COLUMN primary_industry_id BIGINT,
ADD CONSTRAINT fk_companies_primary_industry 
FOREIGN KEY (primary_industry_id) REFERENCES industries(id);

-- Add secondary_industries column (comma-separated list of industry names/tags)
ALTER TABLE companies 
ADD COLUMN secondary_industries TEXT;

-- Add index on primary_industry_id for better performance
CREATE INDEX idx_companies_primary_industry_id ON companies(primary_industry_id); 