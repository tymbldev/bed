-- Migration script to update jobs table for approval system
-- Convert auto_approved boolean to approved integer

-- Step 1: Add the new approved column
ALTER TABLE jobs ADD COLUMN approved INT NOT NULL DEFAULT 0;

-- Step 2: Migrate existing data
-- Convert existing auto_approved=true to approved=1 (APPROVED)
-- Convert existing auto_approved=false to approved=0 (PENDING)
UPDATE jobs SET approved = 1 WHERE auto_approved = true;
UPDATE jobs SET approved = 0 WHERE auto_approved = false;

-- Step 3: Drop the old auto_approved column
ALTER TABLE jobs DROP COLUMN auto_approved;

-- Step 4: Add index for better performance on approval queries
CREATE INDEX idx_jobs_approved ON jobs(approved);
CREATE INDEX idx_jobs_active_approved ON jobs(active, approved);

-- Step 5: Add comment to document the approval values
COMMENT ON COLUMN jobs.approved IS 'Approval status: 0=PENDING, 1=APPROVED, 2=REJECTED'; 