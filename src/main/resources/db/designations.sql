-- SQL script to populate the designations table with a comprehensive list of job titles

-- Clear existing data
TRUNCATE TABLE designations RESTART IDENTITY CASCADE;

-- Insert initial designations
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

-- Engineering Roles
INSERT INTO designations (title) VALUES ('Software Engineer Intern');
INSERT INTO designations (title) VALUES ('Junior Software Engineer');
INSERT INTO designations (title) VALUES ('Staff Engineer');
INSERT INTO designations (title) VALUES ('Distinguished Engineer');
INSERT INTO designations (title) VALUES ('Fellow');
INSERT INTO designations (title) VALUES ('Frontend Engineer');
INSERT INTO designations (title) VALUES ('Backend Engineer');
INSERT INTO designations (title) VALUES ('Full Stack Engineer');
INSERT INTO designations (title) VALUES ('DevOps Engineer');
INSERT INTO designations (title) VALUES ('QA Engineer');
INSERT INTO designations (title) VALUES ('Security Engineer');
INSERT INTO designations (title) VALUES ('Database Administrator');
INSERT INTO designations (title) VALUES ('System Administrator');
INSERT INTO designations (title) VALUES ('Network Engineer');
INSERT INTO designations (title) VALUES ('Mobile Engineer');
INSERT INTO designations (title) VALUES ('Machine Learning Engineer');
INSERT INTO designations (title) VALUES ('Embedded Systems Engineer');
INSERT INTO designations (title) VALUES ('Cloud Engineer');
INSERT INTO designations (title) VALUES ('Release Engineer');
INSERT INTO designations (title) VALUES ('Site Reliability Engineer (SRE)');
INSERT INTO designations (title) VALUES ('Data Architect');
INSERT INTO designations (title) VALUES ('Solution Architect');
INSERT INTO designations (title) VALUES ('Engineering Team Lead');

-- Additional Engineering Roles
INSERT INTO designations (title) VALUES ('Software Development Engineer in Test (SDET)');
INSERT INTO designations (title) VALUES ('Performance Engineer');
INSERT INTO designations (title) VALUES ('AI Engineer');
INSERT INTO designations (title) VALUES ('Computer Vision Engineer');
INSERT INTO designations (title) VALUES ('Robotics Engineer');
INSERT INTO designations (title) VALUES ('Game Developer');
INSERT INTO designations (title) VALUES ('Blockchain Engineer');
INSERT INTO designations (title) VALUES ('Embedded AI Engineer');
INSERT INTO designations (title) VALUES ('Graphics Engineer');
INSERT INTO designations (title) VALUES ('Firmware Engineer');
INSERT INTO designations (title) VALUES ('Enterprise Architect');
INSERT INTO designations (title) VALUES ('Technical Program Manager');
INSERT INTO designations (title) VALUES ('Technical Product Manager');
INSERT INTO designations (title) VALUES ('Data Engineer');
INSERT INTO designations (title) VALUES ('Big Data Engineer');
INSERT INTO designations (title) VALUES ('Data Scientist');
INSERT INTO designations (title) VALUES ('NLP Engineer');

-- Additional Engineering Management Roles
INSERT INTO designations (title) VALUES ('Group Engineering Manager');
INSERT INTO designations (title) VALUES ('Technical Director');
INSERT INTO designations (title) VALUES ('Head of Engineering');
INSERT INTO designations (title) VALUES ('Chief Architect');

-- Specialized Security Roles
INSERT INTO designations (title) VALUES ('Application Security Engineer');
INSERT INTO designations (title) VALUES ('Security Analyst');
INSERT INTO designations (title) VALUES ('Cybersecurity Engineer');
INSERT INTO designations (title) VALUES ('Penetration Tester');
INSERT INTO designations (title) VALUES ('Cloud Security Engineer');

-- Additional Engineering Roles
INSERT INTO designations (title) VALUES ('Test Automation Engineer');
INSERT INTO designations (title) VALUES ('Build Engineer');
INSERT INTO designations (title) VALUES ('Observability Engineer');
INSERT INTO designations (title) VALUES ('Hardware Engineer');
INSERT INTO designations (title) VALUES ('DSP Engineer');
INSERT INTO designations (title) VALUES ('FPGA Engineer');
INSERT INTO designations (title) VALUES ('IoT Engineer');
INSERT INTO designations (title) VALUES ('Edge Computing Engineer');
INSERT INTO designations (title) VALUES ('Augmented Reality Engineer');
INSERT INTO designations (title) VALUES ('Virtual Reality Engineer');
INSERT INTO designations (title) VALUES ('Simulation Engineer');
INSERT INTO designations (title) VALUES ('Autonomous Systems Engineer');
INSERT INTO designations (title) VALUES ('5G Engineer');
INSERT INTO designations (title) VALUES ('Quantum Computing Engineer');

-- Additional Data & AI Roles
INSERT INTO designations (title) VALUES ('AI Research Scientist');
INSERT INTO designations (title) VALUES ('Deep Learning Engineer');
INSERT INTO designations (title) VALUES ('Data Governance Analyst');
INSERT INTO designations (title) VALUES ('Data Visualization Engineer');
INSERT INTO designations (title) VALUES ('Data Operations Engineer');
INSERT INTO designations (title) VALUES ('Synthetic Data Engineer');

-- Cloud & DevOps Expansion
INSERT INTO designations (title) VALUES ('Cloud Solutions Architect');
INSERT INTO designations (title) VALUES ('Cloud Native Engineer');
INSERT INTO designations (title) VALUES ('Platform Engineer');
INSERT INTO designations (title) VALUES ('Infrastructure Engineer');
INSERT INTO designations (title) VALUES ('Chaos Engineer');
INSERT INTO designations (title) VALUES ('Kubernetes Engineer');

-- Cybersecurity Specializations
INSERT INTO designations (title) VALUES ('Incident Response Engineer');
INSERT INTO designations (title) VALUES ('Threat Intelligence Analyst');
INSERT INTO designations (title) VALUES ('Red Team Engineer');
INSERT INTO designations (title) VALUES ('Blue Team Engineer');
INSERT INTO designations (title) VALUES ('Governance, Risk, and Compliance (GRC) Analyst');

-- Specialized Software Engineering Tracks
INSERT INTO designations (title) VALUES ('Software Architect');
INSERT INTO designations (title) VALUES ('Low-Level Systems Engineer');
INSERT INTO designations (title) VALUES ('Compiler Engineer');
INSERT INTO designations (title) VALUES ('API Engineer');

INSERT INTO designations (title) VALUES ('VP of AI');
INSERT INTO designations (title) VALUES ('VP of Data Science');
INSERT INTO designations (title) VALUES ('Chief Data Officer (CDO)');
INSERT INTO designations (title) VALUES ('Chief Information Security Officer (CISO)');
INSERT INTO designations (title) VALUES ('Chief Cloud Officer (CCO)');
INSERT INTO designations (title) VALUES ('Chief Product & Technology Officer (CPTO)');

-- Product Roles
INSERT INTO designations (title) VALUES ('Associate Product Manager');
INSERT INTO designations (title) VALUES ('Product Manager');
INSERT INTO designations (title) VALUES ('Senior Product Manager');
INSERT INTO designations (title) VALUES ('Group Product Manager');
INSERT INTO designations (title) VALUES ('Director of Product');
INSERT INTO designations (title) VALUES ('VP of Product');
INSERT INTO designations (title) VALUES ('Chief Product Officer');
INSERT INTO designations (title) VALUES ('Product Marketing Manager');
INSERT INTO designations (title) VALUES ('Technical Product Manager');
INSERT INTO designations (title) VALUES ('Product Owner');
INSERT INTO designations (title) VALUES ('Product Analyst');
-- Additional Product Roles
INSERT INTO designations (title) VALUES ('Junior Product Manager');
INSERT INTO designations (title) VALUES ('Lead Product Manager');
INSERT INTO designations (title) VALUES ('Principal Product Manager');
INSERT INTO designations (title) VALUES ('Growth Product Manager');
INSERT INTO designations (title) VALUES ('Data Product Manager');
INSERT INTO designations (title) VALUES ('AI Product Manager');
INSERT INTO designations (title) VALUES ('Platform Product Manager');
INSERT INTO designations (title) VALUES ('Mobile Product Manager');
INSERT INTO designations (title) VALUES ('E-commerce Product Manager');
INSERT INTO designations (title) VALUES ('Payments Product Manager');
INSERT INTO designations (title) VALUES ('Security Product Manager');
INSERT INTO designations (title) VALUES ('Cloud Product Manager');
INSERT INTO designations (title) VALUES ('Enterprise Product Manager');
INSERT INTO designations (title) VALUES ('API Product Manager');
INSERT INTO designations (title) VALUES ('IoT Product Manager');
INSERT INTO designations (title) VALUES ('Hardware Product Manager');
INSERT INTO designations (title) VALUES ('EdTech Product Manager');
INSERT INTO designations (title) VALUES ('Healthcare Product Manager');
INSERT INTO designations (title) VALUES ('Gaming Product Manager');
INSERT INTO designations (title) VALUES ('User Experience (UX) Product Manager');
INSERT INTO designations (title) VALUES ('Chief Innovation Officer');
INSERT INTO designations (title) VALUES ('Head of Product Design');
INSERT INTO designations (title) VALUES ('Head of Product Operations');
INSERT INTO designations (title) VALUES ('Chief Growth Officer');

-- Design Roles
INSERT INTO designations (title) VALUES ('UI Designer');
INSERT INTO designations (title) VALUES ('UX Designer');
INSERT INTO designations (title) VALUES ('UI/UX Designer');
INSERT INTO designations (title) VALUES ('Senior Designer');
INSERT INTO designations (title) VALUES ('Design Lead');
INSERT INTO designations (title) VALUES ('Design Manager');
INSERT INTO designations (title) VALUES ('Director of Design');
INSERT INTO designations (title) VALUES ('VP of Design');
INSERT INTO designations (title) VALUES ('Chief Design Officer');
INSERT INTO designations (title) VALUES ('Graphic Designer');
INSERT INTO designations (title) VALUES ('Interaction Designer');
INSERT INTO designations (title) VALUES ('Visual Designer');
INSERT INTO designations (title) VALUES ('UX Researcher');
INSERT INTO designations (title) VALUES ('Information Architect');
INSERT INTO designations (title) VALUES ('Motion Designer');

-- Data Science Roles
INSERT INTO designations (title) VALUES ('Data Analyst');
INSERT INTO designations (title) VALUES ('Data Scientist');
INSERT INTO designations (title) VALUES ('Senior Data Scientist');
INSERT INTO designations (title) VALUES ('Lead Data Scientist');
INSERT INTO designations (title) VALUES ('Data Science Manager');
INSERT INTO designations (title) VALUES ('Director of Data Science');
INSERT INTO designations (title) VALUES ('VP of Data Science');
INSERT INTO designations (title) VALUES ('Chief Data Officer');
INSERT INTO designations (title) VALUES ('Business Intelligence Analyst');
INSERT INTO designations (title) VALUES ('Data Engineer');
INSERT INTO designations (title) VALUES ('AI Researcher');
INSERT INTO designations (title) VALUES ('NLP Engineer');
INSERT INTO designations (title) VALUES ('Computer Vision Engineer');

-- Marketing Roles
INSERT INTO designations (title) VALUES ('Marketing Coordinator');
INSERT INTO designations (title) VALUES ('Marketing Specialist');
INSERT INTO designations (title) VALUES ('Marketing Manager');
INSERT INTO designations (title) VALUES ('Senior Marketing Manager');
