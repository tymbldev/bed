-- Add UUID column to user_resumes table
ALTER TABLE user_resumes ADD COLUMN uuid VARCHAR(36) UNIQUE;

-- Update existing records with UUIDs
UPDATE user_resumes SET uuid = UUID() WHERE uuid IS NULL;

-- Make UUID column NOT NULL
ALTER TABLE user_resumes MODIFY COLUMN uuid VARCHAR(36) NOT NULL;

-- Add index on UUID for faster lookups
CREATE INDEX idx_user_resumes_uuid ON user_resumes(uuid); 