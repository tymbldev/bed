-- Add city and country fields to jobs tables
-- This script adds city_id, city_name, country_id, and country_name columns to job-related tables

-- Add columns to jobs table (common entity)
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS city_name VARCHAR(255);
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS country_name VARCHAR(255);

-- Add columns to external_job_details table (jobs entity)
ALTER TABLE external_job_details ADD COLUMN IF NOT EXISTS city_id BIGINT;
ALTER TABLE external_job_details ADD COLUMN IF NOT EXISTS city_name VARCHAR(255);
ALTER TABLE external_job_details ADD COLUMN IF NOT EXISTS country_id BIGINT;
ALTER TABLE external_job_details ADD COLUMN IF NOT EXISTS country_name VARCHAR(255);

-- Update existing records with default values if needed
-- UPDATE external_job_details SET city_name = 'Unknown' WHERE city_name IS NULL;
-- UPDATE external_job_details SET country_name = 'Unknown' WHERE country_name IS NULL;

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_jobs_city_id ON jobs(city_id);
CREATE INDEX IF NOT EXISTS idx_jobs_country_id ON jobs(country_id);
CREATE INDEX IF NOT EXISTS idx_jobs_city_name ON jobs(city_name);
CREATE INDEX IF NOT EXISTS idx_jobs_country_name ON jobs(country_name);

CREATE INDEX IF NOT EXISTS idx_external_job_details_city_id ON external_job_details(city_id);
CREATE INDEX IF NOT EXISTS idx_external_job_details_country_id ON external_job_details(country_id);
CREATE INDEX IF NOT EXISTS idx_external_job_details_city_name ON external_job_details(city_name);
CREATE INDEX IF NOT EXISTS idx_external_job_details_country_name ON external_job_details(country_name);

-- Add foreign key constraints (optional - uncomment if you want referential integrity)
-- ALTER TABLE jobs ADD CONSTRAINT fk_jobs_city FOREIGN KEY (city_id) REFERENCES cities(id);
-- ALTER TABLE jobs ADD CONSTRAINT fk_jobs_country FOREIGN KEY (country_id) REFERENCES countries(id);
-- ALTER TABLE external_job_details ADD CONSTRAINT fk_external_job_details_city FOREIGN KEY (city_id) REFERENCES cities(id);
-- ALTER TABLE external_job_details ADD CONSTRAINT fk_external_job_details_country FOREIGN KEY (country_id) REFERENCES countries(id);

-- Add comments to document the purpose of these columns
COMMENT ON COLUMN jobs.city_name IS 'Name of the city where the job is located';
COMMENT ON COLUMN jobs.country_name IS 'Name of the country where the job is located';
COMMENT ON COLUMN external_job_details.city_id IS 'ID of the city where the job is located';
COMMENT ON COLUMN external_job_details.city_name IS 'Name of the city where the job is located';
COMMENT ON COLUMN external_job_details.country_id IS 'ID of the country where the job is located';
COMMENT ON COLUMN external_job_details.country_name IS 'Name of the country where the job is located';
