-- Create similar_content table for storing similar names for companies and designations
-- This table helps in mapping external job data to internal entities using fuzzy matching

CREATE TABLE IF NOT EXISTS similar_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_name VARCHAR(500) NOT NULL COMMENT 'The main/original name (e.g., company name or designation)',
    type ENUM('COMPANY', 'DESIGNATION') NOT NULL COMMENT 'Type of content - company or designation',
    similar_name VARCHAR(500) NOT NULL COMMENT 'Similar/variant name that maps to the parent',
    confidence_score DECIMAL(3,2) DEFAULT 0.80 COMMENT 'AI confidence score for the mapping (0.00 to 1.00)',
    source VARCHAR(100) DEFAULT 'AI_MAPPING' COMMENT 'Source of the mapping (AI_MAPPING, MANUAL, etc.)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_parent_name_type (parent_name, type),
    INDEX idx_similar_name_type (similar_name, type),
    INDEX idx_confidence_score (confidence_score),
    INDEX idx_created_at (created_at)
);

-- Add comment to table
COMMENT ON TABLE similar_content IS 'Table for storing similar names for companies and designations to aid in external job data mapping';
