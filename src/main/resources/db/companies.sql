-- SQL script to populate the companies table with major tech companies for interview preparation
-- These INSERT statements will add companies with their basic information

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE companies;
ALTER TABLE companies AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Insert technology companies
INSERT IGNORE INTO companies (name, description, website, logo_url) VALUES
('Google', 'A multinational technology company that specializes in Internet-related services and products.', 'https://www.google.com', 'https://logo.clearbit.com/google.com'),
('Amazon', 'An American multinational technology company focusing on e-commerce, cloud computing, digital streaming, and artificial intelligence.', 'https://www.amazon.com', 'https://logo.clearbit.com/amazon.com'),
('Microsoft', 'An American multinational technology corporation that produces computer software, consumer electronics, personal computers, and related services.', 'https://www.microsoft.com', 'https://logo.clearbit.com/microsoft.com'),
('Apple', 'An American multinational technology company that specializes in consumer electronics, software and online services.', 'https://www.apple.com', 'https://logo.clearbit.com/apple.com'),
('Facebook', 'An American social media and technology company', 'https://www.facebook.com', 'https://logo.clearbit.com/facebook.com'),
('Netflix', 'An American subscription streaming service and production company.', 'https://www.netflix.com', 'https://logo.clearbit.com/netflix.com'),
('Twitter', 'An American microblogging and social networking service.', 'https://www.twitter.com', 'https://logo.clearbit.com/twitter.com'),
('LinkedIn', 'A business and employment-focused social media platform.', 'https://www.linkedin.com', 'https://logo.clearbit.com/linkedin.com'),
('Salesforce', 'An American cloud-based software company that provides customer relationship management service.', 'https://www.salesforce.com', 'https://logo.clearbit.com/salesforce.com'),
('Oracle', 'An American multinational computer technology corporation.', 'https://www.oracle.com', 'https://logo.clearbit.com/oracle.com'),
('IBM', 'An American multinational technology company', 'https://www.ibm.com', 'https://logo.clearbit.com/ibm.com'),
('Adobe', 'An American multinational computer software company.', 'https://www.adobe.com', 'https://logo.clearbit.com/adobe.com'); 