-- Add redirect_url column to external_job_details table
ALTER TABLE external_job_details 
ADD COLUMN IF NOT EXISTS redirect_url TEXT;

-- Add comment for documentation
ALTER TABLE external_job_details 
COMMENT = 'External job details with redirect URL support for AI-fetched jobs';
