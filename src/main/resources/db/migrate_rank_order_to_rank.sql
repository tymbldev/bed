-- Migrate from rank_order to rank column
-- This script adds the new 'rank' column and populates it with meaningful values

-- Add the rank column if it doesn't exist
ALTER TABLE industries ADD COLUMN IF NOT EXISTS rank INT DEFAULT 0;

-- Update industries with meaningful rank values
-- Higher priority industries get lower rank numbers (1, 2, 3...)
UPDATE industries SET rank = 1 WHERE name = 'Information Technology & Services';
UPDATE industries SET rank = 2 WHERE name = 'Software Development';
UPDATE industries SET rank = 3 WHERE name = 'Software as a Service (SaaS)';
UPDATE industries SET rank = 4 WHERE name = 'Cloud Computing';
UPDATE industries SET rank = 5 WHERE name = 'Financial Services';
UPDATE industries SET rank = 6 WHERE name = 'FinTech';
UPDATE industries SET rank = 7 WHERE name = 'Healthcare & HealthTech';
UPDATE industries SET rank = 8 WHERE name = 'E-commerce & Online Retail';
UPDATE industries SET rank = 9 WHERE name = 'Artificial Intelligence & Machine Learning (AI/ML)';
UPDATE industries SET rank = 10 WHERE name = 'Data Analytics & Business Intelligence';
UPDATE industries SET rank = 11 WHERE name = 'Cybersecurity';
UPDATE industries SET rank = 12 WHERE name = 'Mobile Applications';
UPDATE industries SET rank = 13 WHERE name = 'Education Technology (EdTech)';
UPDATE industries SET rank = 14 WHERE name = 'Logistics & Supply Chain';
UPDATE industries SET rank = 15 WHERE name = 'Marketing & Advertising Technology (MarTech/AdTech)';
UPDATE industries SET rank = 16 WHERE name = 'Human Resources & HRTech';
UPDATE industries SET rank = 17 WHERE name = 'Customer Relationship Management (CRM)';
UPDATE industries SET rank = 18 WHERE name = 'Productivity & Collaboration Tools';
UPDATE industries SET rank = 19 WHERE name = 'Project & Work Management Software';
UPDATE industries SET rank = 20 WHERE name = 'Entertainment & Media';
UPDATE industries SET rank = 21 WHERE name = 'Gaming & Fantasy Sports';
UPDATE industries SET rank = 22 WHERE name = 'Social Media & Online Communities';
UPDATE industries SET rank = 23 WHERE name = 'Travel & Hospitality Technology';
UPDATE industries SET rank = 24 WHERE name = 'Automotive & Electric Vehicles';
UPDATE industries SET rank = 25 WHERE name = 'Real Estate & PropTech';
UPDATE industries SET rank = 26 WHERE name = 'Agriculture & AgTech';
UPDATE industries SET rank = 27 WHERE name = 'LegalTech & Contract Management';
UPDATE industries SET rank = 28 WHERE name = 'Retail (Physical / Omnichannel / D2C)';
UPDATE industries SET rank = 29 WHERE name = 'Manufacturing & Industrial Automation';
UPDATE industries SET rank = 30 WHERE name = 'Semiconductors & Electronics';
UPDATE industries SET rank = 31 WHERE name = 'Energy (Oil, Gas, Renewable)';
UPDATE industries SET rank = 32 WHERE name = 'CleanTech & ClimateTech';
UPDATE industries SET rank = 33 WHERE name = 'Construction & Infrastructure';
UPDATE industries SET rank = 34 WHERE name = 'Food & Beverage / D2C';
UPDATE industries SET rank = 35 WHERE name = 'Recruitment & Talent Management';
UPDATE industries SET rank = 36 WHERE name = 'Non-Profit & Development Finance';
UPDATE industries SET rank = 37 WHERE name = 'B2B Platforms & Marketplaces';
UPDATE industries SET rank = 38 WHERE name = 'Business Process Outsourcing (BPO/KPO)';
UPDATE industries SET rank = 39 WHERE name = 'Subscription & Billing Software';
UPDATE industries SET rank = 40 WHERE name = 'Streaming, Video & Media Tech';
UPDATE industries SET rank = 41 WHERE name = 'Transportation & Mobility';
UPDATE industries SET rank = 42 WHERE name = 'Blockchain & Crypto';
UPDATE industries SET rank = 43 WHERE name = 'Scientific & Research Services';
UPDATE industries SET rank = 44 WHERE name = 'Conglomerates / Diversified Enterprises';
UPDATE industries SET rank = 45 WHERE name = 'Aerospace & Defense';
UPDATE industries SET rank = 46 WHERE name = 'Non-Banking Financial Company (NBFC)';
UPDATE industries SET rank = 47 WHERE name = 'Banking & Lending';
UPDATE industries SET rank = 48 WHERE name = 'Insurance & InsurTech';
UPDATE industries SET rank = 49 WHERE name = 'Investment & Wealth Management';
UPDATE industries SET rank = 50 WHERE name = 'Telemedicine & Telehealth';

-- Set rank for any remaining industries that don't have a specific rank
UPDATE industries SET rank = 100 WHERE rank = 0 OR rank IS NULL;

-- Create index on rank for better performance
CREATE INDEX IF NOT EXISTS idx_industries_rank ON industries(rank);

-- Verify the ranking
SELECT id, name, rank FROM industries ORDER BY rank ASC;

-- Show industries without ranks (if any)
SELECT id, name, rank FROM industries WHERE rank = 0 OR rank IS NULL;
