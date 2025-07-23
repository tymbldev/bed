-- Create secondary industry mapping table
-- This table stores mappings between original secondary industry names and their standardized parent categories

CREATE TABLE IF NOT EXISTS secondary_industry_mapping (
    secondary_industry_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(500) NOT NULL COMMENT 'Original secondary industry name from companies table',
    mapped_id BIGINT NOT NULL COMMENT 'Unique ID for the mapped parent category',
    mapped_name VARCHAR(500) NOT NULL COMMENT 'Standardized parent category name',
    processed BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Flag to track if this mapping has been processed',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update timestamp',
    
    -- Indexes for performance
    INDEX idx_name (name),
    INDEX idx_mapped_id (mapped_id),
    INDEX idx_mapped_name (mapped_name),
    INDEX idx_processed (processed),
    INDEX idx_created_at (created_at),
    
    -- Unique constraint on name to prevent duplicates
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Maps secondary industry names to standardized parent categories';

-- Insert some example mappings for testing
INSERT IGNORE INTO secondary_industry_mapping (name, mapped_id, mapped_name, processed) VALUES
('Fortune 500', 1, 'Fortune 500', true),
('Fortune500', 1, 'Fortune 500', true),
('Fortune 500 Top', 1, 'Fortune 500', true),
('F500', 1, 'Fortune 500', true),
('Tech', 2, 'Technology', true),
('IT', 3, 'Information Technology', true),
('Software', 4, 'Software Development', true),
('SaaS', 5, 'Software as a Service', true),
('E-commerce', 6, 'E-commerce', true),
('Ecommerce', 6, 'E-commerce', true),
('Online Retail', 6, 'E-commerce', true),
('FinTech', 7, 'Financial Technology', true),
('Fintech', 7, 'Financial Technology', true),
('Banking', 8, 'Banking & Financial Services', true),
('Finance', 8, 'Banking & Financial Services', true),
('Healthcare', 9, 'Healthcare', true),
('Health Care', 9, 'Healthcare', true),
('Medical', 9, 'Healthcare', true),
('AI/ML', 10, 'Artificial Intelligence & Machine Learning', true),
('Machine Learning', 10, 'Artificial Intelligence & Machine Learning', true),
('Artificial Intelligence', 10, 'Artificial Intelligence & Machine Learning', true); 