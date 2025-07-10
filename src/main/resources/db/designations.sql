-- SQL script to populate the designations table with a comprehensive list of job names

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE designations;
ALTER TABLE designations AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Engineering Roles (Level 1-4)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Software Engineer', 1, true),
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
('Site Reliability Engineer (SRE)', 2, true),
('Application Security Engineer', 2, true),
('Security Analyst', 2, true),
('Test Automation Engineer', 2, true),
('Build Engineer', 2, true),
('Senior Software Engineer', 2, true),
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
('Firmware Engineer', 3, true),
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
('Quantum Computing Engineer', 4, true),
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

-- Security Roles (Level 2-4)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Cybersecurity Engineer', 3, true),
('Penetration Tester', 3, true),
('Cloud Security Engineer', 4, true),
('Incident Response Engineer', 3, true),
('Threat Intelligence Analyst', 3, true),
('Red Team Engineer', 4, true),
('Blue Team Engineer', 4, true),
('Governance, Risk, and Compliance (GRC) Analyst', 3, true);

-- Data & AI Roles (Level 3-5)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('AI Research Scientist', 4, true),
('Deep Learning Engineer', 3, true),
('Data Governance Analyst', 3, true),
('Data Visualization Engineer', 3, true),
('Data Operations Engineer', 3, true),
('Synthetic Data Engineer', 3, true);

-- Cloud & DevOps Roles (Level 3-5)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Cloud Solutions Architect', 5, true),
('Cloud Native Engineer', 3, true),
('Platform Engineer', 3, true),
('Infrastructure Engineer', 3, true),
('Chaos Engineer', 4, true),
('Kubernetes Engineer', 3, true);

-- Specialized Software Roles (Level 3-5)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Software Architect', 5, true),
('Low-Level Systems Engineer', 3, true),
('Compiler Engineer', 4, true),
('API Engineer', 3, true);

-- Executive Roles (Level 10-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('VP of AI', 11, true),
('VP of Data Science', 11, true),
('Chief Data Officer (CDO)', 12, true),
('Chief Information Security Officer (CISO)', 12, true),
('Chief Cloud Officer (CCO)', 12, true),
('Chief Product & Technology Officer (CPTO)', 12, true);

-- Product Roles (Level 1-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
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
('Product Analyst', 1, true),
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
('User Experience (UX) Product Manager', 3, true),
('Product Director', 8, true),
('Product Operations Manager', 5, true);

-- Executive and Head Roles (Level 8-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Chief Innovation Officer', 12, true),
('Head of Product Design', 8, true),
('Head of Product Operations', 8, true),
('Chief Growth Officer', 12, true);

-- Design Roles (Level 1-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
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
('Motion Designer', 2, true),
('Junior Graphic Designer', 1, true),
('Senior Graphic Designer', 3, true),
('Lead Graphic Designer', 4, true),
('Art Director', 4, true),
('Senior UX Designer', 4, true),
('Senior UX/UI Designer', 4, true),
('Senior UI Designer', 4, true),
('Creative Director', 6, true);

-- Data Science Roles (Level 1-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
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
('Data Science Manager', 5, true),
('Data Engineering Lead', 5, true);

-- Marketing Roles (Level 1-4)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Marketing Coordinator', 1, true),
('Marketing Specialist', 2, true),
('Marketing Manager', 3, true),
('Senior Marketing Manager', 4, true),
('Marketing Associate', 1, true),
('Marketing Executive', 2, true),
('Digital Marketing Specialist', 2, true),
('Marketing Director', 5, true),
('VP of Marketing', 11, true),
('Chief Marketing Officer', 12, true),
('Brand Manager', 4, true);

-- Sales Roles (Level 1-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
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
('Solutions Consultant', 3, true),
('Sales Associate', 1, true),
('Sales Executive', 2, true),
('Sales Director', 5, true),
('Account Manager', 3, true);

-- Finance Roles (Level 1-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
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
INSERT IGNORE INTO designations (name, level, enabled) VALUES
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
INSERT IGNORE INTO designations (name, level, enabled) VALUES
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
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Legal Assistant', 1, true),
('Legal Counsel', 3, true),
('Senior Legal Counsel', 4, true),
('Corporate Counsel', 4, true),
('Intellectual Property Counsel', 4, true),
('Director of Legal Affairs', 8, true),
('VP of Legal', 11, true),
('General Counsel', 12, true),
('Compliance Manager', 4, true),
('Privacy Officer', 4, true),
('Legal Associate', 2, true),
('Legal Manager', 4, true),
('Legal Director', 5, true);

-- Research Roles (Level 1-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
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
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Customer Support Representative', 1, true),
('Customer Success Associate', 1, true),
('Customer Success Manager', 3, true),
('Senior Customer Success Manager', 4, true),
('Customer Experience Manager', 4, true),
('Director of Customer Success', 8, true),
('VP of Customer Success', 11, true),
('Chief Customer Officer', 12, true),
('Customer Operations Manager', 4, true),
('Customer Insights Analyst', 2, true),
('Technical Support Specialist', 2, true),
('Customer Support Associate', 2, true),
('Senior Customer Support Specialist', 3, true),
('Customer Support Manager', 4, true),
('Customer Support Lead', 4, true),
('Head of Customer Support', 6, true);

-- Content & Communications Roles (Level 1-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Content Writer', 1, true),
('Technical Writer', 2, true),
('Content Strategist', 3, true),
('Communications Manager', 4, true),
('Content Marketing Manager', 4, true),
('Director of Communications', 8, true),
('VP of Communications', 11, true),
('Chief Communications Officer', 12, true),
('Documentation Manager', 4, true),
('Other', 1, true);

-- Administrative Roles (Level 1-3)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Administrative Assistant', 1, true),
('Executive Assistant', 2, true),
('Office Manager', 3, true);

-- Accounting Roles (Level 1-4)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Accounting Clerk', 1, true),
('Bookkeeper', 2, true),
('Accounting Manager', 3, true),
('Controller', 4, true);

-- Facilities Management Roles (Level 2-4)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Facilities Coordinator', 2, true),
('Director of Facilities', 4, true);

-- Diversity & Inclusion Roles (Level 3-12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Diversity and Inclusion Manager', 3, true),
('Chief Diversity Officer', 12, true);

-- Creative and Design Roles (Level 2-4)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Industrial Designer', 2, true);

-- Public Relations Roles (Level 2-4)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Public Relations Specialist', 2, true),
('Public Relations Manager', 3, true),
('Director of Public Relations', 4, true);

-- Communications Roles (Level 2-4)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Communications Specialist', 2, true),
('Communications Director', 4, true);

-- Legal Roles (Level 2-4)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Legal Secretary', 2, true),
('Paralegal', 2, true),
('Contract Administrator', 3, true),
('Patent Agent', 3, true);

-- Executive Roles (Level 12)
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Chief Legal Officer', 12, true),
('Chief Compliance Officer', 12, true);

-- Additional Engineering Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('AI Systems Engineer', 3, true),
('Big Data Engineer', 3, true),
('Streaming Data Engineer', 3, true),
('Data Platform Engineer', 3, true),
('DataOps Engineer', 3, true),
('MLOps Engineer', 3, true),
('Observability Architect', 5, true),
('ElasticSearch Engineer', 3, true),
('Computer Systems Analyst', 2, true),
('Integration Engineer', 3, true),
('Storage Engineer', 3, true),
('Release Manager', 4, true),
('Virtualization Engineer', 3, true),
('High Performance Computing (HPC) Engineer', 4, true),
('High Frequency Trading Engineer', 4, true),
('ASIC Engineer', 3, true),
('EDA Engineer', 3, true),
('Computer Hardware Engineer', 3, true),
('Information Systems Engineer', 3, true),
('Platform Reliability Engineer', 3, true),
('Observability SRE', 3, true),
('Telemetry Engineer', 3, true),
('Enterprise Architect', 5, true);

-- Education Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Teaching Assistant', 1, true),
('School Teacher', 2, true),
('Primary School Teacher', 2, true),
('High School Teacher', 2, true),
('Special Education Teacher', 2, true),
('Subject Matter Expert (SME)', 3, true),
('Lecturer', 3, true),
('Assistant Professor', 4, true),
('Associate Professor', 5, true),
('Professor', 6, true),
('Head of Department (HOD)', 7, true),
('Academic Dean', 8, true),
('Principal', 8, true),
('Chancellor', 12, true),
('Instructional Designer', 3, true),
('Education Program Manager', 4, true),
('Curriculum Developer', 3, true),
('eLearning Developer', 3, true),
('Online Course Instructor', 3, true),
('Associate Teacher', 1, true),
('Classroom Aide', 1, true),
('Subject Teacher', 2, true),
('Grade Level Teacher', 2, true),
('Lead Teacher', 3, true),
('Master Teacher', 3, true),
('Instructional Coordinator', 4, true),
('Curriculum Specialist', 4, true),
('Academic Coordinator', 4, true),
('Vice Principal', 5, true),
('Director of Education', 8, true),
('Chief Academic Officer', 12, true),
('Superintendent', 12, true),
('Assistant Superintendent', 11, true),
('Dean of Students', 6, true),
('Dean of Academics', 6, true),
('Teacher', 2, true),
('Education Consultant', 4, true),
('Research Scholar', 2, true),
('Tutor', 1, true);

-- Creative and Media Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Copywriter', 2, true),
('Senior Copywriter', 3, true),
('Animator', 2, true),
('3D Artist', 3, true),
('Video Editor', 2, true),
('Film Director', 8, true),
('Sound Designer', 2, true),
('Broadcast Engineer', 3, true),
('Voice Over Artist', 2, true),
('Journalist', 2, true),
('Editor-in-Chief', 8, true),
('Columnist', 3, true),
('Illustrator', 2, true),
('Motion Graphics Artist', 3, true),
('Scriptwriter', 3, true),
('Social Media Manager', 3, true),
('Content Creator', 1, true),
('Influencer Marketing Specialist', 3, true);

-- Healthcare Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Medical Assistant', 1, true),
('Registered Nurse (RN)', 2, true),
('Clinical Research Associate', 3, true),
('Physician', 6, true),
('Surgeon', 7, true),
('Radiologist', 6, true),
('Healthcare Administrator', 5, true),
('Public Health Analyst', 3, true),
('Biomedical Engineer', 3, true),
('Pharmacist', 4, true),
('Genetic Counselor', 4, true),
('Nurse', 2, true),
('Registered Nurse', 3, true),
('Head Nurse', 4, true),
('General Physician', 4, true),
('Specialist Doctor', 6, true),
('Dentist', 5, true),
('Veterinarian', 4, true),
('Physiotherapist', 3, true),
('Lab Technician', 2, true),
('Hospital Administrator', 6, true),
('Doctor', 3, true),
('Senior Doctor', 4, true),
('Medical Director', 8, true),
('VP of Healthcare', 11, true),
('Chief Healthcare Officer', 12, true);

-- Logistics and Supply Chain Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Logistics Coordinator', 2, true),
('Warehouse Manager', 3, true),
('Inventory Analyst', 2, true),
('Procurement Specialist', 2, true),
('Transportation Manager', 4, true),
('Fleet Manager', 4, true),
('Import Export Manager', 4, true),
('Demand Planner', 3, true),
('Supply Chain Analyst', 3, true),
('Warehouse Associate', 1, true),
('Warehouse Supervisor', 3, true),
('Inventory Manager', 3, true),
('Supply Chain Planner', 3, true),
('Logistics Manager', 4, true);

-- Legal Operations Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Legal Intern', 1, true),
('Legal Operations Manager', 4, true),
('Legal Compliance Analyst', 3, true);

-- NGO and Social Work Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Program Officer', 2, true),
('Program Manager', 4, true),
('Field Coordinator', 2, true),
('Community Outreach Officer', 2, true),
('Social Worker', 2, true),
('NGO Project Lead', 4, true),
('Fundraising Manager', 4, true),
('Grants Manager', 4, true);

-- Skilled Trades and Technical Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Electrician', 2, true),
('Plumber', 2, true),
('Carpenter', 2, true),
('Mechanic', 2, true),
('Welder', 2, true),
('Delivery Executive', 1, true),
('Bike Rider', 1, true),
('Cab Driver', 1, true),
('Field Technician', 2, true),
('Security Guard', 1, true),
('Housekeeping Staff', 1, true),
('Construction Worker', 1, true);

-- Additional Technical Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Technical Program Manager', 4, true),
('Software Development Manager', 5, true),
('Observability Platform Engineer', 4, true),
('AI Product Engineer', 3, true),
('Software Craftsman', 3, true),
('CI/CD Engineer', 3, true),
('Test Data Management Engineer', 3, true),
('Reliability Architect', 5, true);

-- Customer Support and BPO Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Customer Support Executive', 1, true),
('Customer Service Representative', 1, true),
('Call Center Executive', 1, true),
('Inbound Call Executive', 1, true),
('Outbound Call Executive', 1, true),
('Email Support Executive', 1, true),
('Chat Support Executive', 1, true),
('Voice Process Executive', 1, true),
('Non-Voice Process Executive', 1, true),
('Customer Care Associate', 1, true),
('Technical Support Executive', 2, true),
('Customer Support Specialist', 2, true),
('Senior Customer Support Executive', 2, true),
('Escalation Specialist', 2, true),
('Customer Experience Executive', 2, true),
('Customer Relationship Executive', 2, true),
('Product Support Executive', 2, true),
('Helpdesk Executive', 1, true),
('L1 Support Engineer', 2, true),
('L2 Support Engineer', 3, true),
('Support Analyst', 2, true),
('Senior Support Analyst', 3, true),
('Customer Support Team Lead', 3, true),
('Technical Support Specialist', 3, true),
('Support Engineer', 3, true),
('Customer Success Lead', 4, true),
('Customer Support Supervisor', 4, true),
('Support Team Manager', 4, true),
('Customer Success Strategist', 4, true),
('Head of Customer Support', 5, true),
('Customer Success Director', 5, true),
('VP of Customer Experience', 6, true),
('VP of Customer Success', 6, true),
('Chief Customer Officer', 7, true),
('Quality Analyst - BPO', 3, true),
('BPO Trainer', 3, true),
('Process Trainer', 3, true),
('Workforce Analyst', 3, true),
('Call Quality Evaluator', 2, true),
('Support Operations Analyst', 3, true),
('Customer Retention Specialist', 3, true),
('Complaint Resolution Specialist', 2, true),
('Client Support Executive', 2, true),
('Support Operations Manager', 4, true),
('Support Transformation Lead', 4, true),
('Support Strategy Analyst', 3, true);

-- Sales and Business Development Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Sales Executive', 1, true),
('Inside Sales Executive', 1, true),
('Field Sales Executive', 1, true),
('Telesales Executive', 1, true),
('Sales Development Representative (SDR)', 2, true),
('Business Development Executive', 2, true),
('Inside Sales Specialist', 2, true),
('Key Account Executive', 2, true),
('Channel Sales Executive', 2, true),
('Territory Sales Executive', 2, true),
('Sales Associate', 1, true),
('Senior Sales Executive', 2, true),
('Business Development Associate', 2, true),
('Area Sales Manager', 3, true),
('Territory Manager', 3, true),
('Key Account Manager', 3, true),
('Channel Sales Manager', 3, true),
('Enterprise Sales Manager', 4, true),
('Regional Sales Manager', 4, true),
('National Sales Manager', 5, true),
('Head of Sales', 5, true),
('Director of Sales', 6, true),
('Chief Sales Officer (CSO)', 7, true);

-- Operations and Supply Chain Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Operations Executive', 1, true),
('Back Office Executive', 1, true),
('Order Processing Executive', 1, true),
('Inventory Executive', 1, true),
('Procurement Executive', 1, true),
('Dispatch Executive', 1, true),
('Fleet Coordinator', 1, true),
('Operations Associate', 2, true),
('Supply Chain Executive', 2, true),
('Warehouse Executive', 2, true),
('Store Executive', 2, true),
('Vendor Management Executive', 2, true),
('Procurement Analyst', 2, true),
('Procurement Manager', 3, true),
('City Operations Manager', 3, true),
('Cluster Operations Manager', 3, true),
('Hub Manager', 3, true),
('Head of Operations', 5, true),
('Chief Operating Officer (COO)', 7, true);

-- HR and Recruitment Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('HR Executive', 1, true),
('Recruitment Executive', 1, true),
('Talent Acquisition Executive', 1, true),
('Payroll Executive', 1, true),
('HR Associate', 2, true),
('Recruiter', 2, true),
('Senior HR Executive', 2, true),
('HR Analyst', 2, true),
('HR Generalist', 2, true),
('L&D Executive', 2, true),
('HR Business Partner (HRBP)', 3, true),
('Talent Acquisition Manager', 3, true),
('L&D Manager', 3, true),
('Employee Relations Manager', 3, true),
('HR Operations Manager', 3, true),
('HRBP Manager', 3, true),
('Senior HR Manager', 4, true),
('Director - Human Resources', 6, true),
('VP - Human Resources', 6, true),
('Chief Human Resources Officer (CHRO)', 7, true);

-- Engineering and Technical Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Mechanical Engineer', 2, true),
('Electrical Engineer', 2, true),
('Civil Engineer', 2, true),
('Chemical Engineer', 2, true),
('Industrial Engineer', 2, true),
('Manufacturing Engineer', 2, true),
('Environmental Engineer', 2, true),
('Aerospace Engineer', 2, true),
('Nuclear Engineer', 3, true),
('Marine Engineer', 2, true),
('Petroleum Engineer', 3, true),
('Structural Engineer', 3, true),
('Geotechnical Engineer', 3, true),
('Materials Engineer', 3, true),
('Acoustics Engineer', 3, true),
('Mechatronics Engineer', 3, true),
('Reliability Engineer', 3, true),
('Safety Engineer', 3, true),
('Engineering Technician', 1, true),
('Engineering Intern', 1, true),
('Engineering Aide', 1, true),
('Apprentice Engineer', 1, true),
('Junior Engineer', 1, true),
('Lead Engineer', 4, true),
('Senior Engineer', 4, true),
('Principal Engineer', 5, true),
('Executive Engineer', 6, true),
('Chief Engineer', 6, true);

-- Consulting Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('Consulting Analyst', 2, true),
('Consulting Associate', 3, true),
('Consultant', 4, true),
('Senior Consultant', 5, true),
('Consulting Manager', 5, true),
('Consulting Director', 8, true),
('VP of Consulting', 11, true),
('Principal Consultant', 5, true),
('Junior Consultant', 2, true),
('Consulting Senior Associate', 3, true),
('Lead Consultant', 6, true),
('Engagement Manager', 6, true),
('Management Consultant', 4, true),
('Strategy Consultant', 4, true),
('Senior Strategy Consultant', 5, true),
('Business Consultant', 3, true),
('Senior Business Consultant', 4, true),
('Change Management Consultant', 4, true),
('Human Resources Consultant', 4, true),
('Senior HR Consultant', 5, true),
('Risk Management Consultant', 4, true),
('Senior Risk Consultant', 5, true),
('Financial Consultant', 4, true),
('Senior Financial Consultant', 5, true),
('Technology Consultant', 4, true),
('Senior Technology Consultant', 5, true),
('Operational Consultant', 4, true),
('Senior Operational Consultant', 5, true),
('Consulting Partner', 10, true),
('Managing Consultant', 7, true),
('Senior Managing Consultant', 8, true),
('Consulting Executive', 9, true),
('Global Consulting Lead', 12, true),
('Head of Consulting', 12, true),
('Global Director of Consulting', 12, true),
('Consulting Intern', 1, true),
('Consulting Assistant', 2, true),
('Junior Strategy Consultant', 3, true),
('Junior Business Consultant', 3, true),
('Lead Strategy Consultant', 6, true),
('Senior Lead Consultant', 7, true),
('Consulting Associate Director', 7, true),
('Director of Strategy Consulting', 8, true),
('Consulting Senior Vice President', 11, true),
('Consulting Executive Director', 9, true),
('Senior Risk Management Consultant', 5, true),
('Executive Strategy Consultant', 10, true),
('Global Business Consultant', 6, true),
('Corporate Consultant', 4, true),
('Corporate Strategy Consultant', 4, true),
('Innovation Consultant', 4, true),
('Senior Innovation Consultant', 5, true),
('Technology Strategy Consultant', 4, true),
('Senior Technology Strategy Consultant', 5, true),
('Senior Operations Consultant', 5, true),
('Business Strategy Consultant', 4, true),
('Transformation Consultant', 4, true),
('Senior Transformation Consultant', 5, true),
('Change Implementation Consultant', 4, true),
('Senior Change Implementation Consultant', 5, true),
('Digital Strategy Consultant', 4, true),
('Senior Digital Strategy Consultant', 5, true),
('Senior Consulting Partner', 10, true),
('Regional Consulting Director', 8, true),
('Global Consulting Partner', 12, true),
('Global Strategy Consultant', 6, true),
('Senior Corporate Consultant', 5, true),
('Senior Financial Strategy Consultant', 5, true);

-- Corporate Development Roles
INSERT IGNORE INTO designations (name, level, enabled) VALUES
('M&A Associate', 2, true),
('M&A Manager', 3, true),
('Director of Corporate Development', 5, true),
('VP of Corporate Development', 11, true);



