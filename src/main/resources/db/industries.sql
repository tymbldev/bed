-- Industries table creation and data population
-- This script creates the industries table and populates it with the provided industry list

-- Create industries table if it doesn't exist
CREATE TABLE IF NOT EXISTS industries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Clear existing data (optional - comment out if you want to keep existing data)
-- DELETE FROM industries;

-- Insert industries data
INSERT INTO industries (name, description) VALUES
('Information Technology & Services', 'Technology and IT services industry'),
('Software Development', 'Software development and programming'),
('Software as a Service (SaaS)', 'Cloud-based software services'),
('Cloud Computing', 'Cloud infrastructure and platform services'),
('Financial Services', 'Banking, insurance, and financial services'),
('Non-Banking Financial Company (NBFC)', 'Non-banking financial institutions'),
('Banking & Lending', 'Traditional banking and lending services'),
('Insurance & InsurTech', 'Insurance and insurance technology'),
('Investment & Wealth Management', 'Investment and wealth management services'),
('Healthcare & HealthTech', 'Healthcare and health technology'),
('Telemedicine & Telehealth', 'Remote healthcare and telemedicine services'),
('Education Technology (EdTech)', 'Educational technology and online learning'),
('E-commerce & Online Retail', 'Online retail and e-commerce platforms'),
('Logistics & Supply Chain', 'Logistics and supply chain management'),
('Travel & Hospitality Technology', 'Travel and hospitality technology'),
('Automotive & Electric Vehicles', 'Automotive and electric vehicle industry'),
('Aerospace & Defense', 'Aerospace and defense industry'),
('Real Estate & PropTech', 'Real estate and property technology'),
('Agriculture & AgTech', 'Agriculture and agricultural technology'),
('Human Resources & HRTech', 'Human resources and HR technology'),
('Marketing & Advertising Technology (MarTech/AdTech)', 'Marketing and advertising technology'),
('Customer Relationship Management (CRM)', 'Customer relationship management software'),
('Cybersecurity', 'Cybersecurity and information security'),
('Artificial Intelligence & Machine Learning (AI/ML)', 'AI and machine learning technologies'),
('Data Analytics & Business Intelligence', 'Data analytics and business intelligence'),
('Mobile Applications', 'Mobile app development and services'),
('Entertainment & Media', 'Entertainment and media industry'),
('Gaming & Fantasy Sports', 'Gaming and fantasy sports platforms'),
('Social Media & Online Communities', 'Social media and online community platforms'),
('Professional Services & Consulting', 'Professional services and consulting'),
('LegalTech & Contract Management', 'Legal technology and contract management'),
('Retail (Physical / Omnichannel / D2C)', 'Physical retail, omnichannel, and direct-to-consumer'),
('Manufacturing & Industrial Automation', 'Manufacturing and industrial automation'),
('Semiconductors & Electronics', 'Semiconductor and electronics industry'),
('Energy (Oil, Gas, Renewable)', 'Energy sector including oil, gas, and renewable energy'),
('CleanTech & ClimateTech', 'Clean technology and climate technology'),
('Construction & Infrastructure', 'Construction and infrastructure development'),
('Food & Beverage / D2C', 'Food and beverage industry including direct-to-consumer'),
('Recruitment & Talent Management', 'Recruitment and talent management services'),
('Non-Profit & Development Finance', 'Non-profit organizations and development finance'),
('B2B Platforms & Marketplaces', 'Business-to-business platforms and marketplaces'),
('Business Process Outsourcing (BPO/KPO)', 'Business process outsourcing and knowledge process outsourcing'),
('Subscription & Billing Software', 'Subscription management and billing software'),
('Productivity & Collaboration Tools', 'Productivity and collaboration software'),
('Project & Work Management Software', 'Project and work management tools'),
('Streaming, Video & Media Tech', 'Streaming, video, and media technology'),
('Transportation & Mobility', 'Transportation and mobility services'),
('Blockchain & Crypto', 'Blockchain technology and cryptocurrency'),
('Scientific & Research Services', 'Scientific research and development services'),
('Conglomerates / Diversified Enterprises', 'Diversified business conglomerates'),
('FinTech', 'Financial technology and digital financial services');

-- Verify the data
SELECT COUNT(*) as total_industries FROM industries; 