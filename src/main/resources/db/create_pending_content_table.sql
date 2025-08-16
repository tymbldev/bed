-- Create pending_content table to track NO_MATCH cases
-- This table stores entities that couldn't be tagged by the system
CREATE TABLE pending_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_name VARCHAR(500) NOT NULL COMMENT 'The name of the entity that couldn\'t be tagged',
    entity_type VARCHAR(50) NOT NULL COMMENT 'Type of entity: COMPANY, DESIGNATION, CITY, COUNTRY',
    source_table VARCHAR(100) NOT NULL COMMENT 'Source table where this entity was found (e.g., external_job_details)',
    source_id BIGINT COMMENT 'ID from the source table for reference',
    portal_name VARCHAR(100) COMMENT 'Portal name if from external job crawling',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When this NO_MATCH was recorded',
    processed_at TIMESTAMP NULL COMMENT 'When this was last processed/attempted',
    attempt_count INT DEFAULT 0 COMMENT 'Number of times processing was attempted',
    notes TEXT COMMENT 'Additional notes about why it couldn\'t be tagged',
    INDEX idx_entity_type (entity_type),
    INDEX idx_entity_name (entity_name),
    INDEX idx_source_table (source_table),
    INDEX idx_created_at (created_at),
    INDEX idx_processed_at (processed_at)
) COMMENT='Tracks entities that couldn\'t be tagged by the system for future analysis';
