-- Update industry ranks with meaningful values
-- This script assigns ranks to industries based on their importance and current order

-- First, add the rank_order column if it doesn't exist
ALTER TABLE industries ADD COLUMN IF NOT EXISTS rank_order INT DEFAULT 0;

-- Update industries with meaningful rank values
-- Higher priority industries get lower rank numbers (1, 2, 3...)
UPDATE industries SET rank_order = 1 WHERE name = 'Information Technology & Services';
UPDATE industries SET rank_order = 2 WHERE name = 'Software Development';
UPDATE industries SET rank_order = 3 WHERE name = 'Software as a Service (SaaS)';
UPDATE industries SET rank_order = 4 WHERE name = 'Cloud Computing';
UPDATE industries SET rank_order = 5 WHERE name = 'Financial Services';
UPDATE industries SET rank_order = 6 WHERE name = 'FinTech';
UPDATE industries SET rank_order = 7 WHERE name = 'Healthcare & HealthTech';
UPDATE industries SET rank_order = 8 WHERE name = 'E-commerce & Online Retail';
UPDATE industries SET rank_order = 9 WHERE name = 'Artificial Intelligence & Machine Learning (AI/ML)';
UPDATE industries SET rank_order = 10 WHERE name = 'Data Analytics & Business Intelligence';
UPDATE industries SET rank_order = 11 WHERE name = 'Cybersecurity';
UPDATE industries SET rank_order = 12 WHERE name = 'Mobile Applications';
UPDATE industries SET rank_order = 13 WHERE name = 'Education Technology (EdTech)';
UPDATE industries SET rank_order = 14 WHERE name = 'Logistics & Supply Chain';
UPDATE industries SET rank_order = 15 WHERE name = 'Marketing & Advertising Technology (MarTech/AdTech)';
UPDATE industries SET rank_order = 16 WHERE name = 'Human Resources & HRTech';
UPDATE industries SET rank_order = 17 WHERE name = 'Customer Relationship Management (CRM)';
UPDATE industries SET rank_order = 18 WHERE name = 'Productivity & Collaboration Tools';
UPDATE industries SET rank_order = 19 WHERE name = 'Project & Work Management Software';
UPDATE industries SET rank_order = 20 WHERE name = 'Entertainment & Media';
UPDATE industries SET rank_order = 21 WHERE name = 'Gaming & Fantasy Sports';
UPDATE industries SET rank_order = 22 WHERE name = 'Social Media & Online Communities';
UPDATE industries SET rank_order = 23 WHERE name = 'Travel & Hospitality Technology';
UPDATE industries SET rank_order = 24 WHERE name = 'Automotive & Electric Vehicles';
UPDATE industries SET rank_order = 25 WHERE name = 'Real Estate & PropTech';
UPDATE industries SET rank_order = 26 WHERE name = 'Agriculture & AgTech';
UPDATE industries SET rank_order = 27 WHERE name = 'LegalTech & Contract Management';
UPDATE industries SET rank_order = 28 WHERE name = 'Retail (Physical / Omnichannel / D2C)';
UPDATE industries SET rank_order = 29 WHERE name = 'Manufacturing & Industrial Automation';
UPDATE industries SET rank_order = 30 WHERE name = 'Semiconductors & Electronics';
UPDATE industries SET rank_order = 31 WHERE name = 'Energy (Oil, Gas, Renewable)';
UPDATE industries SET rank_order = 32 WHERE name = 'CleanTech & ClimateTech';
UPDATE industries SET rank_order = 33 WHERE name = 'Construction & Infrastructure';
UPDATE industries SET rank_order = 34 WHERE name = 'Food & Beverage / D2C';
UPDATE industries SET rank_order = 35 WHERE name = 'Recruitment & Talent Management';
UPDATE industries SET rank_order = 36 WHERE name = 'Non-Profit & Development Finance';
UPDATE industries SET rank_order = 37 WHERE name = 'B2B Platforms & Marketplaces';
UPDATE industries SET rank_order = 38 WHERE name = 'Business Process Outsourcing (BPO/KPO)';
UPDATE industries SET rank_order = 39 WHERE name = 'Subscription & Billing Software';
UPDATE industries SET rank_order = 40 WHERE name = 'Streaming, Video & Media Tech';
UPDATE industries SET rank_order = 41 WHERE name = 'Transportation & Mobility';
UPDATE industries SET rank_order = 42 WHERE name = 'Blockchain & Crypto';
UPDATE industries SET rank_order = 43 WHERE name = 'Scientific & Research Services';
UPDATE industries SET rank_order = 44 WHERE name = 'Conglomerates / Diversified Enterprises';
UPDATE industries SET rank_order = 45 WHERE name = 'Aerospace & Defense';
UPDATE industries SET rank_order = 46 WHERE name = 'Non-Banking Financial Company (NBFC)';
UPDATE industries SET rank_order = 47 WHERE name = 'Banking & Lending';
UPDATE industries SET rank_order = 48 WHERE name = 'Insurance & InsurTech';
UPDATE industries SET rank_order = 49 WHERE name = 'Investment & Wealth Management';
UPDATE industries SET rank_order = 50 WHERE name = 'Telemedicine & Telehealth';

-- Set rank for any remaining industries that don't have a specific rank
UPDATE industries SET rank_order = 100 WHERE rank_order = 0 OR rank_order IS NULL;

-- Create index on rank_order for better performance
CREATE INDEX IF NOT EXISTS idx_industries_rank_order ON industries(rank_order);

-- Verify the ranking
SELECT id, name, rank_order FROM industries ORDER BY rank_order ASC;

-- Show industries without ranks (if any)
SELECT id, name, rank_order FROM industries WHERE rank_order = 0 OR rank_order IS NULL;
