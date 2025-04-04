-- SQL script to populate the designations table with a comprehensive list of job titles

-- Clear existing data
TRUNCATE TABLE designations RESTART IDENTITY CASCADE;

-- Insert initial designations with explicit hierarchy
INSERT INTO designations (title, level, enabled) VALUES
('Software Engineer', 1, true),
('Senior Software Engineer', 2, true),
('Lead Software Engineer', 3, true),
('Principal Software Engineer', 4, true),
('Architect', 5, true),
('Senior Architect', 6, true),
('Technical Lead', 7, true),
('Engineering Manager', 8, true),
('Senior Engineering Manager', 9, true),
('Director of Engineering', 10, true),
('VP of Engineering', 11, true),
('CTO', 12, true);

-- Engineering Roles (Level 1-2)
INSERT INTO designations (title, level, enabled) VALUES
('Software Engineer Intern', 1, true),
('Junior Software Engineer', 1, true),
('Frontend Engineer', 1, true),
('Backend Engineer', 1, true),
('Full Stack Engineer', 1, true),
('DevOps Engineer', 1, true),
('QA Engineer', 1, true),
('Security Engineer', 1, true),
('Database Administrator', 2, true),
('System Administrator', 2, true),
('Network Engineer', 2, true),
('Mobile Engineer', 2, true),
('Machine Learning Engineer', 2, true),
('Embedded Systems Engineer', 2, true),
('Cloud Engineer', 2, true),
('Release Engineer', 2, true),
('Site Reliability Engineer (SRE)', 2, true);

-- Senior Level Roles (Level 3-4)
INSERT INTO designations (title, level, enabled) VALUES
('Staff Engineer', 3, true),
('Distinguished Engineer', 4, true),
('Fellow', 4, true),
('Data Architect', 3, true),
('Solution Architect', 3, true),
('Engineering Team Lead', 3, true),
('Software Development Engineer in Test (SDET)', 3, true),
('Performance Engineer', 3, true),
('AI Engineer', 3, true),
('Computer Vision Engineer', 3, true),
('Robotics Engineer', 3, true),
('Game Developer', 3, true),
('Blockchain Engineer', 3, true),
('Embedded AI Engineer', 3, true),
('Graphics Engineer', 3, true),
('Firmware Engineer', 3, true);

-- Architecture and Management Roles (Level 5-8)
INSERT INTO designations (title, level, enabled) VALUES
('Enterprise Architect', 5, true),
('Technical Program Manager', 5, true),
('Technical Product Manager', 5, true),
('Data Engineer', 5, true),
('Big Data Engineer', 5, true),
('Data Scientist', 5, true),
('NLP Engineer', 5, true),
('Group Engineering Manager', 6, true),
('Technical Director', 7, true),
('Head of Engineering', 8, true),
('Chief Architect', 8, true);

-- Security Roles (Level 2-4)
INSERT INTO designations (title, level, enabled) VALUES
('Application Security Engineer', 2, true),
('Security Analyst', 2, true),
('Cybersecurity Engineer', 3, true),
('Penetration Tester', 3, true),
('Cloud Security Engineer', 4, true);

-- Specialized Engineering Roles (Level 2-4)
INSERT INTO designations (title, level, enabled) VALUES
('Test Automation Engineer', 2, true),
('Build Engineer', 2, true),
('Observability Engineer', 3, true),
('Hardware Engineer', 3, true),
('DSP Engineer', 3, true),
('FPGA Engineer', 3, true),
('IoT Engineer', 3, true),
('Edge Computing Engineer', 3, true),
('Augmented Reality Engineer', 3, true),
('Virtual Reality Engineer', 3, true),
('Simulation Engineer', 3, true),
('Autonomous Systems Engineer', 4, true),
('5G Engineer', 3, true),
('Quantum Computing Engineer', 4, true);

-- Data & AI Roles (Level 3-5)
INSERT INTO designations (title, level, enabled) VALUES
('AI Research Scientist', 4, true),
('Deep Learning Engineer', 3, true),
('Data Governance Analyst', 3, true),
('Data Visualization Engineer', 3, true),
('Data Operations Engineer', 3, true),
('Synthetic Data Engineer', 3, true);

-- Cloud & DevOps Roles (Level 3-5)
INSERT INTO designations (title, level, enabled) VALUES
('Cloud Solutions Architect', 5, true),
('Cloud Native Engineer', 3, true),
('Platform Engineer', 3, true),
('Infrastructure Engineer', 3, true),
('Chaos Engineer', 4, true),
('Kubernetes Engineer', 3, true);

-- Additional Security Roles (Level 3-4)
INSERT INTO designations (title, level, enabled) VALUES
('Incident Response Engineer', 3, true),
('Threat Intelligence Analyst', 3, true),
('Red Team Engineer', 4, true),
('Blue Team Engineer', 4, true),
('Governance, Risk, and Compliance (GRC) Analyst', 3, true);

-- Specialized Software Roles (Level 3-5)
INSERT INTO designations (title, level, enabled) VALUES
('Software Architect', 5, true),
('Low-Level Systems Engineer', 3, true),
('Compiler Engineer', 4, true),
('API Engineer', 3, true);

-- Executive Roles (Level 10-12)
INSERT INTO designations (title, level, enabled) VALUES
('VP of AI', 11, true),
('VP of Data Science', 11, true),
('Chief Data Officer (CDO)', 12, true),
('Chief Information Security Officer (CISO)', 12, true),
('Chief Cloud Officer (CCO)', 12, true),
('Chief Product & Technology Officer (CPTO)', 12, true);

-- Product Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('Associate Product Manager', 1, true),
('Product Manager', 2, true),
('Senior Product Manager', 3, true),
('Group Product Manager', 4, true),
('Director of Product', 8, true),
('VP of Product', 11, true),
('Chief Product Officer', 12, true),
('Product Marketing Manager', 2, true),
('Technical Product Manager', 3, true),
('Product Owner', 2, true),
('Product Analyst', 1, true);

-- Additional Product Roles (Level 1-4)
INSERT INTO designations (title, level, enabled) VALUES
('Junior Product Manager', 1, true),
('Lead Product Manager', 3, true),
('Principal Product Manager', 4, true),
('Growth Product Manager', 3, true),
('Data Product Manager', 3, true),
('AI Product Manager', 3, true),
('Platform Product Manager', 3, true),
('Mobile Product Manager', 3, true),
('E-commerce Product Manager', 3, true),
('Payments Product Manager', 3, true),
('Security Product Manager', 3, true),
('Cloud Product Manager', 3, true),
('Enterprise Product Manager', 3, true),
('API Product Manager', 3, true),
('IoT Product Manager', 3, true),
('Hardware Product Manager', 3, true),
('EdTech Product Manager', 3, true),
('Healthcare Product Manager', 3, true),
('Gaming Product Manager', 3, true),
('User Experience (UX) Product Manager', 3, true);

-- Executive and Head Roles (Level 8-12)
INSERT INTO designations (title, level, enabled) VALUES
('Chief Innovation Officer', 12, true),
('Head of Product Design', 8, true),
('Head of Product Operations', 8, true),
('Chief Growth Officer', 12, true);

-- Design Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('UI Designer', 1, true),
('UX Designer', 1, true),
('UI/UX Designer', 2, true),
('Senior Designer', 3, true),
('Design Lead', 4, true),
('Design Manager', 6, true),
('Director of Design', 8, true),
('VP of Design', 11, true),
('Chief Design Officer', 12, true),
('Graphic Designer', 1, true),
('Interaction Designer', 2, true),
('Visual Designer', 2, true),
('UX Researcher', 2, true),
('Information Architect', 3, true),
('Motion Designer', 2, true);

-- Data Science Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('Data Analyst', 1, true),
('Data Scientist', 2, true),
('Senior Data Scientist', 3, true),
('Lead Data Scientist', 4, true),
('Data Science Manager', 6, true),
('Director of Data Science', 8, true),
('VP of Data Science', 11, true),
('Chief Data Officer', 12, true),
('Business Intelligence Analyst', 2, true),
('Data Engineer', 2, true),
('AI Researcher', 3, true),
('NLP Engineer', 3, true),
('Computer Vision Engineer', 3, true);

-- Marketing Roles (Level 1-4)
INSERT INTO designations (title, level, enabled) VALUES
('Marketing Coordinator', 1, true),
('Marketing Specialist', 2, true),
('Marketing Manager', 3, true),
('Senior Marketing Manager', 4, true);

-- Sales Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('Sales Development Representative', 1, true),
('Account Executive', 2, true),
('Senior Account Executive', 3, true),
('Enterprise Account Executive', 4, true),
('Sales Manager', 4, true),
('Senior Sales Manager', 5, true),
('Regional Sales Director', 8, true),
('VP of Sales', 11, true),
('Chief Sales Officer', 12, true),
('Sales Operations Analyst', 2, true),
('Sales Engineer', 3, true),
('Solutions Consultant', 3, true);

-- Finance Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('Financial Analyst', 1, true),
('Senior Financial Analyst', 2, true),
('Finance Manager', 3, true),
('Financial Controller', 4, true),
('Treasury Manager', 4, true),
('Director of Finance', 8, true),
('VP of Finance', 11, true),
('Chief Financial Officer', 12, true),
('Investment Analyst', 2, true),
('Risk Analyst', 2, true),
('Compliance Officer', 3, true);

-- HR Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('HR Coordinator', 1, true),
('HR Specialist', 2, true),
('HR Business Partner', 3, true),
('Senior HR Business Partner', 4, true),
('HR Manager', 4, true),
('Talent Acquisition Specialist', 2, true),
('Learning & Development Manager', 4, true),
('Compensation & Benefits Manager', 4, true),
('Director of HR', 8, true),
('VP of Human Resources', 11, true),
('Chief Human Resources Officer', 12, true);

-- Operations Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('Operations Coordinator', 1, true),
('Operations Analyst', 2, true),
('Operations Manager', 3, true),
('Senior Operations Manager', 4, true),
('Business Operations Manager', 4, true),
('Director of Operations', 8, true),
('VP of Operations', 11, true),
('Chief Operations Officer', 12, true),
('Process Improvement Specialist', 2, true),
('Facilities Manager', 3, true),
('Supply Chain Manager', 4, true);

-- Legal Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('Legal Assistant', 1, true),
('Legal Counsel', 3, true),
('Senior Legal Counsel', 4, true),
('Corporate Counsel', 4, true),
('Intellectual Property Counsel', 4, true),
('Director of Legal Affairs', 8, true),
('VP of Legal', 11, true),
('General Counsel', 12, true),
('Compliance Manager', 4, true),
('Privacy Officer', 4, true);

-- Research Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('Research Assistant', 1, true),
('Research Associate', 2, true),
('Senior Researcher', 3, true),
('Principal Researcher', 4, true),
('Research Team Lead', 4, true),
('Director of Research', 8, true),
('VP of Research', 11, true),
('Chief Research Officer', 12, true),
('Research Program Manager', 4, true),
('Innovation Researcher', 3, true);

-- Customer Success Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('Customer Support Representative', 1, true),
('Customer Success Associate', 1, true),
('Customer Success Manager', 3, true),
('Senior Customer Success Manager', 4, true),
('Customer Experience Manager', 4, true),
('Director of Customer Success', 8, true),
('VP of Customer Success', 11, true),
('Chief Customer Officer', 12, true),
('Customer Operations Manager', 4, true),
('Customer Insights Analyst', 2, true);

-- Content & Communications Roles (Level 1-12)
INSERT INTO designations (title, level, enabled) VALUES
('Content Writer', 1, true),
('Technical Writer', 2, true),
('Content Strategist', 3, true),
('Communications Manager', 4, true),
('Content Marketing Manager', 4, true),
('Director of Communications', 8, true),
('VP of Communications', 11, true),
('Chief Communications Officer', 12, true),
('Documentation Manager', 4, true),
('Brand Manager', 4, true);
