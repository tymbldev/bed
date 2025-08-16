-- Add sync flag to external_job_details table for tracking which jobs have been synced to the main Job table

ALTER TABLE external_job_details ADD COLUMN IF NOT EXISTS is_synced_to_job_table BOOLEAN DEFAULT FALSE;

-- Add index for better performance when querying unsynced jobs
CREATE INDEX IF NOT EXISTS idx_external_job_details_sync_status ON external_job_details(is_synced_to_job_table);

-- Add comment to document the purpose of this column
COMMENT ON COLUMN external_job_details.is_synced_to_job_table IS 'Flag indicating if this external job has been synced to the main jobs table';
