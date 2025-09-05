-- Migration script to add web crawling support for external job details
-- This script adds the new table for storing crawled content and updates the existing table

-- Create the new table for storing crawled content from company portals
CREATE TABLE IF NOT EXISTS external_job_details_from_company_portal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_job_detail_id BIGINT NOT NULL,
    redirect_url TEXT NOT NULL,
    raw_html_content LONGTEXT,
    parsed_text_content LONGTEXT,
    crawl_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    crawl_duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Add foreign key constraint
    CONSTRAINT fk_external_job_detail_id 
        FOREIGN KEY (external_job_detail_id) 
        REFERENCES external_job_details(id) 
        ON DELETE CASCADE,
    
    -- Add index for performance
    INDEX idx_external_job_detail_id (external_job_detail_id),
    INDEX idx_crawl_status (crawl_status),
    INDEX idx_created_at (created_at)
);

-- Add crawl_status column to existing external_job_details table
ALTER TABLE external_job_details 
ADD COLUMN IF NOT EXISTS crawl_status VARCHAR(20) NOT NULL DEFAULT 'NOT_CRAWLED' 
COMMENT 'Status of web crawling: NOT_CRAWLED, CRAWLING, CRAWLED, FAILED';

-- Add index for the new column
CREATE INDEX IF NOT EXISTS idx_crawl_status ON external_job_details(crawl_status);

-- Update existing records to have the default crawl status
UPDATE external_job_details 
SET crawl_status = 'NOT_CRAWLED' 
WHERE crawl_status IS NULL;

-- Add comments for documentation
ALTER TABLE external_job_details_from_company_portal 
COMMENT = 'Stores crawled content from company job portals for external job details';

ALTER TABLE external_job_details 
MODIFY COLUMN crawl_status VARCHAR(20) NOT NULL DEFAULT 'NOT_CRAWLED' 
COMMENT 'Status of web crawling: NOT_CRAWLED, CRAWLING, CRAWLED, FAILED';
