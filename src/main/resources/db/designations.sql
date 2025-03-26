-- SQL script to populate the designations table with a comprehensive list of job titles

-- Clear existing data
TRUNCATE TABLE designations RESTART IDENTITY CASCADE;

-- Engineering Roles
INSERT INTO designations (title, description, level) VALUES ('Software Engineer Intern', 'Entry-level software engineering role for students or recent graduates', 1);
INSERT INTO designations (title, description, level) VALUES ('Junior Software Engineer', 'Entry-level software development', 2);
INSERT INTO designations (title, description, level) VALUES ('Software Engineer', 'Software development and implementation', 3);
INSERT INTO designations (title, description, level) VALUES ('Senior Software Engineer', 'Advanced software development and technical leadership', 4);
INSERT INTO designations (title, description, level) VALUES ('Staff Engineer', 'Technical leadership across multiple teams or projects', 5);
INSERT INTO designations (title, description, level) VALUES ('Principal Engineer', 'Top-level individual contributor in engineering', 6);
INSERT INTO designations (title, description, level) VALUES ('Distinguished Engineer', 'Expert-level engineer with organization-wide impact', 7);
INSERT INTO designations (title, description, level) VALUES ('Fellow', 'Highest level of technical expertise', 8);
INSERT INTO designations (title, description, level) VALUES ('Frontend Engineer', 'Specialized in user interface development', 3);
INSERT INTO designations (title, description, level) VALUES ('Backend Engineer', 'Specialized in server-side development', 3);
INSERT INTO designations (title, description, level) VALUES ('Full Stack Engineer', 'Proficient in both frontend and backend development', 3);
INSERT INTO designations (title, description, level) VALUES ('DevOps Engineer', 'Manages development and operations infrastructure', 3);
INSERT INTO designations (title, description, level) VALUES ('QA Engineer', 'Quality assurance and testing', 3);
INSERT INTO designations (title, description, level) VALUES ('Security Engineer', 'Specialized in application and infrastructure security', 3);
INSERT INTO designations (title, description, level) VALUES ('Database Administrator', 'Database design, optimization, and maintenance', 3);
INSERT INTO designations (title, description, level) VALUES ('System Administrator', 'System infrastructure management and maintenance', 3);
INSERT INTO designations (title, description, level) VALUES ('Network Engineer', 'Network infrastructure design and maintenance', 3);
INSERT INTO designations (title, description, level) VALUES ('Mobile Engineer', 'Mobile application development', 3);
INSERT INTO designations (title, description, level) VALUES ('Machine Learning Engineer', 'Machine learning model development and deployment', 4);
INSERT INTO designations (title, description, level) VALUES ('Embedded Systems Engineer', 'Development of embedded systems', 3);
INSERT INTO designations (title, description, level) VALUES ('Cloud Engineer', 'Cloud infrastructure and services management', 3);
INSERT INTO designations (title, description, level) VALUES ('Release Engineer', 'Software release management', 3);
INSERT INTO designations (title, description, level) VALUES ('Site Reliability Engineer (SRE)', 'Ensures reliability and performance of systems', 3);
INSERT INTO designations (title, description, level) VALUES ('Data Architect', 'Design of data systems', 4);
INSERT INTO designations (title, description, level) VALUES ('Solution Architect', 'Designing and implementing solutions', 4);
INSERT INTO designations (title, description, level) VALUES ('Engineering Team Lead', 'Leadership for a small engineering team', 4);

-- Engineering Management
INSERT INTO designations (title, description, level) VALUES ('Engineering Team Lead', 'Leadership for a small engineering team', 4);
INSERT INTO designations (title, description, level) VALUES ('Engineering Manager', 'Manages engineering teams and processes', 5);
INSERT INTO designations (title, description, level) VALUES ('Senior Engineering Manager', 'Manages multiple engineering teams', 6);
INSERT INTO designations (title, description, level) VALUES ('Director of Engineering', 'Strategic leadership for engineering department', 7);
INSERT INTO designations (title, description, level) VALUES ('VP of Engineering', 'Executive leadership for engineering organization', 8);
INSERT INTO designations (title, description, level) VALUES ('CTO', 'Chief Technology Officer - top technical executive', 9);

-- Additional Engineering Roles
INSERT INTO designations (title, description, level) VALUES ('Software Development Engineer in Test (SDET)', 'Specialized in automated testing and quality assurance', 3);
INSERT INTO designations (title, description, level) VALUES ('Performance Engineer', 'Focuses on optimizing system performance', 3);
INSERT INTO designations (title, description, level) VALUES ('AI Engineer', 'Develops artificial intelligence models and applications', 4);
INSERT INTO designations (title, description, level) VALUES ('Computer Vision Engineer', 'Specialized in image processing and computer vision applications', 4);
INSERT INTO designations (title, description, level) VALUES ('Robotics Engineer', 'Develops robotic systems and automation solutions', 4);
INSERT INTO designations (title, description, level) VALUES ('Game Developer', 'Designs and develops video games', 3);
INSERT INTO designations (title, description, level) VALUES ('Blockchain Engineer', 'Develops blockchain applications and smart contracts', 4);
INSERT INTO designations (title, description, level) VALUES ('Embedded AI Engineer', 'Integrates AI into embedded systems', 4);
INSERT INTO designations (title, description, level) VALUES ('Graphics Engineer', 'Develops graphics rendering and visualization systems', 4);
INSERT INTO designations (title, description, level) VALUES ('Firmware Engineer', 'Develops low-level software for hardware devices', 3);
INSERT INTO designations (title, description, level) VALUES ('Enterprise Architect', 'Designs IT strategy and enterprise architecture', 6);
INSERT INTO designations (title, description, level) VALUES ('Technical Program Manager', 'Manages technical projects and engineering programs', 5);
INSERT INTO designations (title, description, level) VALUES ('Technical Product Manager', 'Focuses on product management with technical expertise', 5);
INSERT INTO designations (title, description, level) VALUES ('Data Engineer', 'Designs and maintains data pipelines', 3);
INSERT INTO designations (title, description, level) VALUES ('Big Data Engineer', 'Works with large-scale data processing systems', 4);
INSERT INTO designations (title, description, level) VALUES ('Data Scientist', 'Applies statistical analysis and machine learning to data', 4);
INSERT INTO designations (title, description, level) VALUES ('NLP Engineer', 'Specialized in Natural Language Processing', 4);

-- Additional Engineering Management Roles
INSERT INTO designations (title, description, level) VALUES ('Group Engineering Manager', 'Oversees multiple teams within engineering', 6);
INSERT INTO designations (title, description, level) VALUES ('Technical Director', 'Responsible for technical vision and strategy', 7);
INSERT INTO designations (title, description, level) VALUES ('Head of Engineering', 'Leads engineering at an organizational level', 8);
INSERT INTO designations (title, description, level) VALUES ('Chief Architect', 'Defines architecture vision and best practices', 8);

-- Specialized Security Roles
INSERT INTO designations (title, description, level) VALUES ('Application Security Engineer', 'Focuses on securing software applications', 3);
INSERT INTO designations (title, description, level) VALUES ('Security Analyst', 'Monitors and analyzes security threats', 3);
INSERT INTO designations (title, description, level) VALUES ('Cybersecurity Engineer', 'Designs and implements security solutions', 4);
INSERT INTO designations (title, description, level) VALUES ('Penetration Tester', 'Ethical hacker focused on security testing', 4);
INSERT INTO designations (title, description, level) VALUES ('Cloud Security Engineer', 'Secures cloud-based infrastructure', 4);

-- Additional Engineering Roles
INSERT INTO designations (title, description, level) VALUES ('Test Automation Engineer', 'Develops automated testing frameworks and scripts', 3);
INSERT INTO designations (title, description, level) VALUES ('Build Engineer', 'Manages build automation and CI/CD pipelines', 3);
INSERT INTO designations (title, description, level) VALUES ('Observability Engineer', 'Focuses on monitoring, logging, and system insights', 3);
INSERT INTO designations (title, description, level) VALUES ('Hardware Engineer', 'Designs and develops hardware components', 4);
INSERT INTO designations (title, description, level) VALUES ('DSP Engineer', 'Specializes in Digital Signal Processing', 4);
INSERT INTO designations (title, description, level) VALUES ('FPGA Engineer', 'Develops and programs Field Programmable Gate Arrays', 4);
INSERT INTO designations (title, description, level) VALUES ('IoT Engineer', 'Develops solutions for Internet of Things devices', 3);
INSERT INTO designations (title, description, level) VALUES ('Edge Computing Engineer', 'Focuses on computing at the network edge', 4);
INSERT INTO designations (title, description, level) VALUES ('Augmented Reality Engineer', 'Develops AR applications and systems', 4);
INSERT INTO designations (title, description, level) VALUES ('Virtual Reality Engineer', 'Develops VR applications and immersive experiences', 4);
INSERT INTO designations (title, description, level) VALUES ('Simulation Engineer', 'Builds simulation models and environments', 4);
INSERT INTO designations (title, description, level) VALUES ('Autonomous Systems Engineer', 'Works on self-driving and autonomous systems', 4);
INSERT INTO designations (title, description, level) VALUES ('5G Engineer', 'Focuses on 5G network and infrastructure development', 4);
INSERT INTO designations (title, description, level) VALUES ('Quantum Computing Engineer', 'Develops algorithms and applications for quantum computing', 5);

-- Additional Data & AI Roles
INSERT INTO designations (title, description, level) VALUES ('AI Research Scientist', 'Conducts research in artificial intelligence', 5);
INSERT INTO designations (title, description, level) VALUES ('Deep Learning Engineer', 'Specialized in deep learning model development', 4);
INSERT INTO designations (title, description, level) VALUES ('Data Governance Analyst', 'Ensures data policies, compliance, and integrity', 3);
INSERT INTO designations (title, description, level) VALUES ('Data Visualization Engineer', 'Creates data dashboards and visual analytics', 3);
INSERT INTO designations (title, description, level) VALUES ('Data Operations Engineer', 'Manages data workflows and infrastructure', 3);
INSERT INTO designations (title, description, level) VALUES ('Synthetic Data Engineer', 'Generates synthetic data for AI training', 4);

-- Cloud & DevOps Expansion
INSERT INTO designations (title, description, level) VALUES ('Cloud Solutions Architect', 'Designs cloud-based solutions', 5);
INSERT INTO designations (title, description, level) VALUES ('Cloud Native Engineer', 'Specialized in cloud-native application development', 4);
INSERT INTO designations (title, description, level) VALUES ('Platform Engineer', 'Develops internal platforms and infrastructure', 4);
INSERT INTO designations (title, description, level) VALUES ('Infrastructure Engineer', 'Builds and maintains IT infrastructure', 4);
INSERT INTO designations (title, description, level) VALUES ('Chaos Engineer', 'Focuses on system resilience through controlled failures', 4);
INSERT INTO designations (title, description, level) VALUES ('Kubernetes Engineer', 'Manages container orchestration with Kubernetes', 4);

-- Cybersecurity Specializations
INSERT INTO designations (title, description, level) VALUES ('Incident Response Engineer', 'Handles security incidents and forensic analysis', 4);
INSERT INTO designations (title, description, level) VALUES ('Threat Intelligence Analyst', 'Analyzes and responds to cyber threats', 4);
INSERT INTO designations (title, description, level) VALUES ('Red Team Engineer', 'Performs offensive security assessments', 4);
INSERT INTO designations (title, description, level) VALUES ('Blue Team Engineer', 'Defensive security specialist', 4);
INSERT INTO designations (title, description, level) VALUES ('Governance, Risk, and Compliance (GRC) Analyst', 'Ensures security policies and regulatory compliance', 4);

-- Specialized Software Engineering Tracks
INSERT INTO designations (title, description, level) VALUES ('Software Architect', 'Designs complex software systems and architectures', 5);
INSERT INTO designations (title, description, level) VALUES ('Low-Level Systems Engineer', 'Develops operating systems and system-level software', 4);
INSERT INTO designations (title, description, level) VALUES ('Compiler Engineer', 'Works on programming language compilers and tools', 4);
INSERT INTO designations (title, description, level) VALUES ('API Engineer', 'Designs and develops APIs for software integration', 3);

INSERT INTO designations (title, description, level) VALUES ('VP of AI', 'Leads AI strategy and innovation', 8);
INSERT INTO designations (title, description, level) VALUES ('VP of Data Science', 'Oversees data science initiatives', 8);
INSERT INTO designations (title, description, level) VALUES ('Chief Data Officer (CDO)', 'Top executive responsible for data strategy', 9);
INSERT INTO designations (title, description, level) VALUES ('Chief Information Security Officer (CISO)', 'Oversees cybersecurity strategy and compliance', 9);
INSERT INTO designations (title, description, level) VALUES ('Chief Cloud Officer (CCO)', 'Leads cloud strategy and operations', 9);
INSERT INTO designations (title, description, level) VALUES ('Chief Product & Technology Officer (CPTO)', 'Combines product and tech leadership', 9);

-- Product Roles
INSERT INTO designations (title, description, level) VALUES ('Associate Product Manager', 'Entry-level product management', 2);
INSERT INTO designations (title, description, level) VALUES ('Product Manager', 'Product strategy, roadmap, and execution', 3);
INSERT INTO designations (title, description, level) VALUES ('Senior Product Manager', 'Advanced product strategy and leadership', 4);
INSERT INTO designations (title, description, level) VALUES ('Group Product Manager', 'Leadership for a product group or area', 5);
INSERT INTO designations (title, description, level) VALUES ('Director of Product', 'Strategic leadership for product organization', 6);
INSERT INTO designations (title, description, level) VALUES ('VP of Product', 'Executive leadership for product organization', 7);
INSERT INTO designations (title, description, level) VALUES ('Chief Product Officer', 'Top executive for product strategy', 9);
INSERT INTO designations (title, description, level) VALUES ('Product Marketing Manager', 'Product positioning, messaging, and go-to-market', 3);
INSERT INTO designations (title, description, level) VALUES ('Technical Product Manager', 'Product management with technical focus', 3);
INSERT INTO designations (title, description, level) VALUES ('Product Owner', 'Agile methodology product owner', 3);
INSERT INTO designations (title, description, level) VALUES ('Product Analyst', 'Analyzing product performance and data', 2);
-- Additional Product Roles
INSERT INTO designations (title, description, level) VALUES ('Junior Product Manager', 'Assists in product management tasks and execution', 2);
INSERT INTO designations (title, description, level) VALUES ('Lead Product Manager', 'Leads product development and cross-functional teams', 4);
INSERT INTO designations (title, description, level) VALUES ('Principal Product Manager', 'Senior individual contributor in product management', 5);
INSERT INTO designations (title, description, level) VALUES ('Growth Product Manager', 'Focuses on customer acquisition, retention, and engagement', 4);
INSERT INTO designations (title, description, level) VALUES ('Data Product Manager', 'Manages data-driven products and analytics tools', 4);
INSERT INTO designations (title, description, level) VALUES ('AI Product Manager', 'Manages AI/ML-powered products and features', 4);
INSERT INTO designations (title, description, level) VALUES ('Platform Product Manager', 'Oversees internal platforms and developer tools', 4);
INSERT INTO designations (title, description, level) VALUES ('Mobile Product Manager', 'Manages mobile-first product experiences', 4);
INSERT INTO designations (title, description, level) VALUES ('E-commerce Product Manager', 'Focuses on online retail and commerce products', 4);
INSERT INTO designations (title, description, level) VALUES ('Payments Product Manager', 'Oversees payment systems and financial technology products', 4);
INSERT INTO designations (title, description, level) VALUES ('Security Product Manager', 'Leads product strategy for security-focused offerings', 4);
INSERT INTO designations (title, description, level) VALUES ('Cloud Product Manager', 'Manages cloud-based services and infrastructure products', 4);
INSERT INTO designations (title, description, level) VALUES ('Enterprise Product Manager', 'Manages large-scale B2B/enterprise products', 4);
INSERT INTO designations (title, description, level) VALUES ('API Product Manager', 'Owns API development, strategy, and adoption', 4);
INSERT INTO designations (title, description, level) VALUES ('IoT Product Manager', 'Focuses on Internet of Things (IoT) products', 4);
INSERT INTO designations (title, description, level) VALUES ('Hardware Product Manager', 'Manages hardware and embedded system products', 4);
INSERT INTO designations (title, description, level) VALUES ('EdTech Product Manager', 'Specializes in education technology products', 4);
INSERT INTO designations (title, description, level) VALUES ('Healthcare Product Manager', 'Manages digital health and medical tech products', 4);
INSERT INTO designations (title, description, level) VALUES ('Gaming Product Manager', 'Manages video game and interactive entertainment products', 4);
INSERT INTO designations (title, description, level) VALUES ('User Experience (UX) Product Manager', 'Focuses on user experience and usability improvements', 4);
INSERT INTO designations (title, description, level) VALUES ('Chief Innovation Officer', 'Oversees innovation strategy and new product development', 8);
INSERT INTO designations (title, description, level) VALUES ('Head of Product Design', 'Leads product design strategy and teams', 6);
INSERT INTO designations (title, description, level) VALUES ('Head of Product Operations', 'Oversees processes and efficiency in product teams', 6);
INSERT INTO designations (title, description, level) VALUES ('Chief Growth Officer', 'Responsible for scaling product adoption and revenue', 8);

-- Design Roles
INSERT INTO designations (title, description, level) VALUES ('UI Designer', 'User interface design', 2);
INSERT INTO designations (title, description, level) VALUES ('UX Designer', 'User experience design', 2);
INSERT INTO designations (title, description, level) VALUES ('UI/UX Designer', 'Combined user interface and experience design', 3);
INSERT INTO designations (title, description, level) VALUES ('Senior Designer', 'Advanced design skills and leadership', 4);
INSERT INTO designations (title, description, level) VALUES ('Design Lead', 'Leadership for design team or projects', 5);
INSERT INTO designations (title, description, level) VALUES ('Design Manager', 'Management of design team', 5);
INSERT INTO designations (title, description, level) VALUES ('Director of Design', 'Strategic leadership for design organization', 6);
INSERT INTO designations (title, description, level) VALUES ('VP of Design', 'Executive leadership for design organization', 7);
INSERT INTO designations (title, description, level) VALUES ('Chief Design Officer', 'Top executive for design strategy', 9);
INSERT INTO designations (title, description, level) VALUES ('Graphic Designer', 'Visual design and graphics creation', 2);
INSERT INTO designations (title, description, level) VALUES ('Interaction Designer', 'Specialized in user interactions', 3);
INSERT INTO designations (title, description, level) VALUES ('Visual Designer', 'Visual aesthetics and branding', 3);
INSERT INTO designations (title, description, level) VALUES ('UX Researcher', 'User research and testing', 3);
INSERT INTO designations (title, description, level) VALUES ('Information Architect', 'Structure and organization of information', 3);
INSERT INTO designations (title, description, level) VALUES ('Motion Designer', 'Animation and motion graphics', 3);

-- Data Science Roles
INSERT INTO designations (title, description, level) VALUES ('Data Analyst', 'Data analysis and reporting', 2);
INSERT INTO designations (title, description, level) VALUES ('Data Scientist', 'Statistical analysis and machine learning', 3);
INSERT INTO designations (title, description, level) VALUES ('Senior Data Scientist', 'Advanced data science and leadership', 4);
INSERT INTO designations (title, description, level) VALUES ('Lead Data Scientist', 'Leadership for data science team or projects', 5);
INSERT INTO designations (title, description, level) VALUES ('Data Science Manager', 'Management of data science team', 5);
INSERT INTO designations (title, description, level) VALUES ('Director of Data Science', 'Strategic leadership for data science organization', 6);
INSERT INTO designations (title, description, level) VALUES ('VP of Data Science', 'Executive leadership for data science organization', 7);
INSERT INTO designations (title, description, level) VALUES ('Chief Data Officer', 'Top executive for data strategy', 9);
INSERT INTO designations (title, description, level) VALUES ('Business Intelligence Analyst', 'Business intelligence reporting and analysis', 2);
INSERT INTO designations (title, description, level) VALUES ('Data Engineer', 'Data pipeline and infrastructure development', 3);
INSERT INTO designations (title, description, level) VALUES ('AI Researcher', 'Artificial intelligence research', 4);
INSERT INTO designations (title, description, level) VALUES ('NLP Engineer', 'Natural Language Processing engineering', 3);
INSERT INTO designations (title, description, level) VALUES ('Computer Vision Engineer', 'Computer Vision engineering', 3);

-- Marketing Roles
INSERT INTO designations (title, description, level) VALUES ('Marketing Coordinator', 'Entry-level marketing support', 1);
INSERT INTO designations (title, description, level) VALUES ('Marketing Specialist', 'Specialized marketing functions', 2);
INSERT INTO designations (title, description, level) VALUES ('Marketing Manager', 'Management of marketing programs', 3);
INSERT INTO designations (title, description, level) VALUES ('Senior Marketing Manager', 'Advanced marketing management', 4);
