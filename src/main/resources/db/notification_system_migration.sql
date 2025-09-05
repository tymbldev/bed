-- Migration script for new notification system
-- This script completely replaces the old notification system

-- Drop existing notifications table if it exists
DROP TABLE IF EXISTS notifications;

-- Create new notifications table with updated schema
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('INCOMPLETE_PROFILE', 'COMPANY_JOBS', 'APPLICATION_STATUS', 'POSTED_JOB_APPLICATIONS') NOT NULL,
    message TEXT NOT NULL,
    metadata TEXT,
    seen BOOLEAN NOT NULL DEFAULT FALSE,
    clicked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    seen_at TIMESTAMP NULL,
    clicked_at TIMESTAMP NULL,
    related_entity_id BIGINT NULL,
    related_entity_type VARCHAR(50) NULL,
    
    -- Indexes for performance
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at),
    INDEX idx_seen (seen),
    INDEX idx_clicked (clicked),
    INDEX idx_user_type_created (user_id, type, created_at),
    INDEX idx_related_entity (related_entity_id, related_entity_type),
    
    -- Foreign key constraints (if users table exists)
    -- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add comments for documentation
ALTER TABLE notifications COMMENT = 'New notification system supporting multiple notification types with seen/clicked tracking';

-- Create indexes for common query patterns
CREATE INDEX idx_user_seen_created ON notifications(user_id, seen, created_at);
CREATE INDEX idx_user_type_seen ON notifications(user_id, type, seen);
CREATE INDEX idx_cleanup ON notifications(created_at);

-- Insert sample data for testing (optional - remove in production)
-- INSERT INTO notifications (user_id, type, message, metadata, related_entity_id, related_entity_type) VALUES
-- (1, 'COMPANY_JOBS', '100+ jobs available in Microsoft, act as a referrer.', '{"companyName":"Microsoft","jobCount":100}', NULL, 'company'),
-- (1, 'APPLICATION_STATUS', 'Your application for Software Engineer has been shortlisted.', '{"applicationId":1,"status":"shortlisted","jobTitle":"Software Engineer"}', 1, 'application'),
-- (1, 'POSTED_JOB_APPLICATIONS', '5 new application(s) for your job Senior Developer.', '{"jobId":2,"jobTitle":"Senior Developer","applicationCount":5}', 2, 'job');
