-- SQL script to populate the company_designation_skills table
-- These INSERT statements will add relationships between companies, designations, and skills

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE company_designation_skills;
ALTER TABLE company_designation_skills AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Amazon - Software Engineer - Java
-- Note: Amazon (company_id = 2), Software Engineer (designation_id = 1), Java (skill_id = 1)
INSERT IGNORE INTO company_designation_skills (company_id, designation_id, skill_id, importance_level) VALUES 
(2, 1, 1, 'HIGH'),  -- Amazon - Software Engineer - Java
(2, 1, 2, 'HIGH'),  -- Amazon - Software Engineer - Spring Boot
(2, 1, 5, 'MEDIUM'),  -- Amazon - Software Engineer - JavaScript
(2, 1, 6, 'MEDIUM'),  -- Amazon - Software Engineer - React
(2, 1, 9, 'HIGH'),  -- Amazon - Software Engineer - AWS
(2, 1, 17, 'HIGH'),  -- Amazon - Software Engineer - Data Structures
(2, 1, 18, 'HIGH'),  -- Amazon - Software Engineer - Algorithms
(2, 1, 23, 'MEDIUM'),  -- Amazon - Software Engineer - Design Patterns

-- Google - Software Engineer
(1, 1, 1, 'MEDIUM'),  -- Google - Software Engineer - Java
(1, 1, 3, 'HIGH'),  -- Google - Software Engineer - Python
(1, 1, 5, 'MEDIUM'),  -- Google - Software Engineer - JavaScript
(1, 1, 8, 'HIGH'),  -- Google - Software Engineer - Go
(1, 1, 10, 'MEDIUM'),  -- Google - Software Engineer - GCP
(1, 1, 17, 'HIGH'),  -- Google - Software Engineer - Data Structures
(1, 1, 18, 'HIGH'),  -- Google - Software Engineer - Algorithms
(1, 1, 23, 'MEDIUM'),  -- Google - Software Engineer - Design Patterns

-- Microsoft - Software Engineer
(3, 1, 1, 'MEDIUM'),  -- Microsoft - Software Engineer - Java
(3, 1, 4, 'HIGH'),  -- Microsoft - Software Engineer - C#
(3, 1, 5, 'MEDIUM'),  -- Microsoft - Software Engineer - JavaScript
(3, 1, 11, 'HIGH'),  -- Microsoft - Software Engineer - Azure
(3, 1, 17, 'HIGH'),  -- Microsoft - Software Engineer - Data Structures
(3, 1, 18, 'HIGH'),  -- Microsoft - Software Engineer - Algorithms
(3, 1, 23, 'MEDIUM'),  -- Microsoft - Software Engineer - Design Patterns

-- Amazon - Senior Software Engineer
(2, 2, 1, 'HIGH'),  -- Amazon - Senior Software Engineer - Java
(2, 2, 2, 'HIGH'),  -- Amazon - Senior Software Engineer - Spring Boot
(2, 2, 5, 'MEDIUM'),  -- Amazon - Senior Software Engineer - JavaScript
(2, 2, 6, 'MEDIUM'),  -- Amazon - Senior Software Engineer - React
(2, 2, 9, 'HIGH'),  -- Amazon - Senior Software Engineer - AWS
(2, 2, 17, 'HIGH'),  -- Amazon - Senior Software Engineer - Data Structures
(2, 2, 18, 'HIGH'),  -- Amazon - Senior Software Engineer - Algorithms
(2, 2, 23, 'HIGH'),  -- Amazon - Senior Software Engineer - Design Patterns
(2, 2, 24, 'HIGH'),  -- Amazon - Senior Software Engineer - System Design
(2, 2, 25, 'MEDIUM'),  -- Amazon - Senior Software Engineer - Microservices
(2, 2, 26, 'HIGH'),  -- Amazon - Senior Software Engineer - Distributed Systems

-- Google - Senior Software Engineer
(1, 2, 1, 'MEDIUM'),  -- Google - Senior Software Engineer - Java
(1, 2, 3, 'HIGH'),  -- Google - Senior Software Engineer - Python
(1, 2, 5, 'MEDIUM'),  -- Google - Senior Software Engineer - JavaScript
(1, 2, 8, 'HIGH'),  -- Google - Senior Software Engineer - Go
(1, 2, 10, 'HIGH'),  -- Google - Senior Software Engineer - GCP
(1, 2, 17, 'HIGH'),  -- Google - Senior Software Engineer - Data Structures
(1, 2, 18, 'HIGH'),  -- Google - Senior Software Engineer - Algorithms
(1, 2, 23, 'HIGH'),  -- Google - Senior Software Engineer - Design Patterns
(1, 2, 24, 'HIGH'),  -- Google - Senior Software Engineer - System Design
(1, 2, 25, 'HIGH'),  -- Google - Senior Software Engineer - Microservices
(1, 2, 26, 'HIGH');  -- Google - Senior Software Engineer - Distributed Systems 