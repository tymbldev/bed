-- Notifications table schema
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    is_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    read_at TIMESTAMP NULL,
    firebase_token VARCHAR(500),
    error_message TEXT,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_sent (is_sent),
    INDEX idx_is_read (is_read),
    INDEX idx_type (type),
    INDEX idx_related_entity (related_entity_id, related_entity_type)
);

-- Add foreign key constraint if users table exists
-- ALTER TABLE notifications ADD CONSTRAINT fk_notifications_user_id FOREIGN KEY (user_id) REFERENCES users(id);

-- Insert sample notifications for testing
INSERT INTO notifications (user_id, title, message, type, related_entity_id, related_entity_type, is_read, is_sent, created_at) VALUES
(1, 'New Referral Application', 'Someone applied for the referral you posted for Software Engineer position at Google', 'REFERRAL_APPLICATION', 1, 'JOB_APPLICATION', false, true, NOW() - INTERVAL 2 HOUR),
(1, 'Application Shortlisted!', 'Congratulations! Your application for Senior Developer position at Microsoft has been shortlisted.', 'APPLICATION_SHORTLISTED', 2, 'JOB_APPLICATION', true, true, NOW() - INTERVAL 1 DAY),
(2, 'New Referral Application', 'Someone applied for the referral you posted for Product Manager position at Amazon', 'REFERRAL_APPLICATION', 3, 'JOB_APPLICATION', false, false, NOW() - INTERVAL 30 MINUTE),
(3, 'Application Status Update', 'Your application for Data Scientist position at Netflix has been rejected.', 'APPLICATION_REJECTED', 4, 'JOB_APPLICATION', false, true, NOW() - INTERVAL 3 HOUR),
(1, 'Application Status Update', 'Your application for Frontend Developer position at Facebook has been accepted.', 'APPLICATION_ACCEPTED', 5, 'JOB_APPLICATION', false, false, NOW() - INTERVAL 15 MINUTE); 