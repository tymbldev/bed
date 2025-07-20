-- Create url_contents table for storing extracted content from job URLs
CREATE TABLE IF NOT EXISTS url_contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(1000) NOT NULL UNIQUE,
    extracted_text LONGTEXT,
    extraction_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    extracted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_url (url),
    INDEX idx_extraction_status (extraction_status),
    INDEX idx_extracted_at (extracted_at)
); 