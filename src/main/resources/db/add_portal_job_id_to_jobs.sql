-- Add portalJobId column to jobs table for syncing with ExternalJobDetails
-- This column will store the portal-specific job ID from external job sources

ALTER TABLE jobs ADD COLUMN IF NOT EXISTS portal_job_id VARCHAR(255);

-- Add index for better performance when querying by portal job ID
CREATE INDEX IF NOT EXISTS idx_jobs_portal_job_id ON jobs(portal_job_id);

-- Add a flag to track which jobs have been synced from external sources
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS is_synced_from_external BOOLEAN DEFAULT FALSE;

-- Add index for the sync flag
CREATE INDEX IF NOT EXISTS idx_jobs_synced_from_external ON jobs(is_synced_from_external);

-- Add comment to document the purpose of these columns
COMMENT ON COLUMN jobs.portal_job_id IS 'Portal-specific job ID from external job sources (e.g., LinkedIn, Foundit)';
COMMENT ON COLUMN jobs.is_synced_from_external IS 'Flag indicating if this job was synced from external job sources';
