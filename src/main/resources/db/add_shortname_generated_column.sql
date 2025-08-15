-- Add department_id column to designations table
ALTER TABLE designations ADD COLUMN IF NOT EXISTS department_id BIGINT;

-- Add foreign key constraint to departments table
ALTER TABLE designations 
ADD CONSTRAINT fk_designations_department 
FOREIGN KEY (department_id) REFERENCES departments(id);

-- Add index for better performance
CREATE INDEX IF NOT EXISTS idx_designations_department_id ON designations(department_id);

-- Add a flag to track if department has been assigned
ALTER TABLE designations ADD COLUMN IF NOT EXISTS department_assigned BOOLEAN DEFAULT FALSE;
