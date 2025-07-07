-- Migration script to fix jobs table schema mismatch
-- This script updates the jobs table to match the current entity structure

-- Step 1: Add missing columns if they don't exist
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS city_id BIGINT;
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS country_id BIGINT;
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS designation_id BIGINT;
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS designation VARCHAR(255);
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS salary DECIMAL(10,2);
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS currency_id BIGINT;
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS company VARCHAR(255);
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;

-- Step 2: Update salary column to NOT NULL if it exists but is NULL
UPDATE jobs SET salary = 0 WHERE salary IS NULL;
ALTER TABLE jobs MODIFY COLUMN salary DECIMAL(10,2) NOT NULL;

-- Step 3: Update currency_id column to NOT NULL if it exists but is NULL
UPDATE jobs SET currency_id = 1 WHERE currency_id IS NULL;
ALTER TABLE jobs MODIFY COLUMN currency_id BIGINT NOT NULL;

-- Step 4: Replace auto_approved with approved if auto_approved exists
-- First check if auto_approved column exists
SET @auto_approved_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'jobs' 
    AND COLUMN_NAME = 'auto_approved'
);

-- If auto_approved exists, migrate the data
SET @sql = IF(@auto_approved_exists > 0,
    'ALTER TABLE jobs ADD COLUMN approved INT NOT NULL DEFAULT 0;
     UPDATE jobs SET approved = 1 WHERE auto_approved = true;
     UPDATE jobs SET approved = 0 WHERE auto_approved = false;
     ALTER TABLE jobs DROP COLUMN auto_approved;',
    'SELECT "auto_approved column does not exist, skipping migration" as message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 5: Add foreign key constraints if they don't exist
-- Note: These might fail if the referenced tables don't exist or data is inconsistent
-- You may need to handle these manually based on your specific database state

-- Step 6: Create job_skills table if it doesn't exist
CREATE TABLE IF NOT EXISTS job_skills (
    job_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (job_id, skill_id),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id)
);

-- Step 7: Create job_tags table if it doesn't exist
CREATE TABLE IF NOT EXISTS job_tags (
    job_id BIGINT NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (job_id, tag),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

-- Step 8: Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_jobs_approved ON jobs(approved);
CREATE INDEX IF NOT EXISTS idx_jobs_active_approved ON jobs(active, approved);
CREATE INDEX IF NOT EXISTS idx_jobs_company_id ON jobs(company_id);
CREATE INDEX IF NOT EXISTS idx_jobs_posted_by_id ON jobs(posted_by_id); 