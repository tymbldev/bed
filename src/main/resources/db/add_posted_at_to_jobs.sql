-- Add posted_at column to jobs table
-- This column stores when the job was originally posted (from external portal)
ALTER TABLE jobs ADD COLUMN posted_at TIMESTAMP NULL;

-- Add index for better query performance on posted_at
CREATE INDEX idx_jobs_posted_at ON jobs(posted_at);

-- Update existing jobs to have posted_at = created_at for backward compatibility
UPDATE jobs SET posted_at = created_at WHERE posted_at IS NULL;
