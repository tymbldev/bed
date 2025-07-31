-- Add logo URL and website fetch status flags to companies table
-- These flags track the status of AI-powered logo URL and website fetching

ALTER TABLE companies 
ADD COLUMN logo_url_fetched INT NOT NULL DEFAULT 0 COMMENT '0 = not tried, 1 = fetched, 2 = failed',
ADD COLUMN website_fetched INT NOT NULL DEFAULT 0 COMMENT '0 = not tried, 1 = fetched, 2 = failed';

-- Add indexes for efficient querying
CREATE INDEX idx_companies_logo_url_fetched ON companies(logo_url_fetched);
CREATE INDEX idx_companies_website_fetched ON companies(website_fetched);

-- Add comments to document the flag values
ALTER TABLE companies 
MODIFY COLUMN logo_url_fetched INT NOT NULL DEFAULT 0 COMMENT '0 = not tried, 1 = fetched, 2 = failed',
MODIFY COLUMN website_fetched INT NOT NULL DEFAULT 0 COMMENT '0 = not tried, 1 = fetched, 2 = failed'; 