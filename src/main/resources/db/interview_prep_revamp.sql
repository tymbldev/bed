-- Interview Preparation Revamp - Flat Schema (No Foreign Keys)
-- Drop existing tables if they exist
DROP TABLE IF EXISTS company_designation_skills;
DROP TABLE IF EXISTS company_interview_guides;
DROP TABLE IF EXISTS interview_questions;
DROP TABLE IF EXISTS interview_topics;

-- Create new flat schema tables

-- 1. Interview Topics (Skills) by Designation
CREATE TABLE interview_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    designation VARCHAR(255) NOT NULL,
    topic_name VARCHAR(255) NOT NULL,
    topic_description TEXT,
    difficulty_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'INTERMEDIATE',
    category VARCHAR(100) DEFAULT 'TECHNICAL',
    estimated_prep_time_hours INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_designation (designation),
    INDEX idx_difficulty (difficulty_level),
    INDEX idx_category (category)
);

-- 2. General Interview Questions (Designation-wise, no company)
CREATE TABLE general_interview_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    designation VARCHAR(255) NOT NULL,
    topic_name VARCHAR(255) NOT NULL,
    question_text TEXT NOT NULL,
    answer_text LONGTEXT NOT NULL,
    difficulty_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'INTERMEDIATE',
    question_type ENUM('THEORETICAL', 'PRACTICAL', 'BEHAVIORAL', 'PROBLEM_SOLVING', 'SYSTEM_DESIGN') DEFAULT 'THEORETICAL',
    tags VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_designation (designation),
    INDEX idx_topic (topic_name),
    INDEX idx_difficulty (difficulty_level),
    INDEX idx_type (question_type)
);

-- 3. Company-Specific Interview Questions
CREATE TABLE company_interview_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    designation VARCHAR(255) NOT NULL,
    topic_name VARCHAR(255) NOT NULL,
    question_text TEXT NOT NULL,
    answer_text LONGTEXT NOT NULL,
    difficulty_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'INTERMEDIATE',
    question_type ENUM('THEORETICAL', 'PRACTICAL', 'BEHAVIORAL', 'PROBLEM_SOLVING', 'SYSTEM_DESIGN') DEFAULT 'THEORETICAL',
    company_context TEXT,
    tags VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_company (company_name),
    INDEX idx_designation (designation),
    INDEX idx_topic (topic_name),
    INDEX idx_difficulty (difficulty_level),
    INDEX idx_type (question_type)
);

-- 4. Question Generation Queue (for tracking GenAI generation requests)
CREATE TABLE question_generation_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_type ENUM('GENERAL', 'COMPANY_SPECIFIC') NOT NULL,
    designation VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    topic_name VARCHAR(255),
    difficulty_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'INTERMEDIATE',
    num_questions INT DEFAULT 5,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_request_type (request_type),
    INDEX idx_designation (designation)
);

-- Insert sample topics for common designations
INSERT INTO interview_topics (designation, topic_name, topic_description, difficulty_level, category, estimated_prep_time_hours) VALUES
-- Software Engineer
('Software Engineer', 'Data Structures & Algorithms', 'Core programming concepts, arrays, linked lists, trees, graphs, sorting, searching', 'INTERMEDIATE', 'TECHNICAL', 20),
('Software Engineer', 'System Design', 'Scalable architecture, microservices, databases, caching, load balancing', 'ADVANCED', 'TECHNICAL', 30),
('Software Engineer', 'Database Design', 'SQL, NoSQL, normalization, indexing, query optimization', 'INTERMEDIATE', 'TECHNICAL', 15),
('Software Engineer', 'Object-Oriented Programming', 'Classes, inheritance, polymorphism, encapsulation, design patterns', 'INTERMEDIATE', 'TECHNICAL', 12),
('Software Engineer', 'Web Technologies', 'HTML, CSS, JavaScript, REST APIs, HTTP protocols', 'BEGINNER', 'TECHNICAL', 10),
('Software Engineer', 'Version Control', 'Git, branching strategies, code review processes', 'BEGINNER', 'TECHNICAL', 5),
('Software Engineer', 'Testing', 'Unit testing, integration testing, TDD, mocking frameworks', 'INTERMEDIATE', 'TECHNICAL', 8),
('Software Engineer', 'Agile Methodologies', 'Scrum, Kanban, sprint planning, user stories', 'BEGINNER', 'BEHAVIORAL', 6),

-- Senior Software Engineer
('Senior Software Engineer', 'Advanced System Design', 'Distributed systems, scalability patterns, fault tolerance, consistency models', 'ADVANCED', 'TECHNICAL', 40),
('Senior Software Engineer', 'Performance Optimization', 'Code profiling, memory management, caching strategies, database optimization', 'ADVANCED', 'TECHNICAL', 25),
('Senior Software Engineer', 'Security Best Practices', 'Authentication, authorization, encryption, secure coding practices', 'INTERMEDIATE', 'TECHNICAL', 15),
('Senior Software Engineer', 'Team Leadership', 'Code review, mentoring, technical decision making, project planning', 'INTERMEDIATE', 'BEHAVIORAL', 20),
('Senior Software Engineer', 'Architecture Patterns', 'Microservices, event-driven architecture, CQRS, domain-driven design', 'ADVANCED', 'TECHNICAL', 30),

-- Data Scientist
('Data Scientist', 'Machine Learning Fundamentals', 'Supervised/unsupervised learning, model evaluation, feature engineering', 'INTERMEDIATE', 'TECHNICAL', 25),
('Data Scientist', 'Statistical Analysis', 'Hypothesis testing, regression, probability, statistical inference', 'INTERMEDIATE', 'TECHNICAL', 20),
('Data Scientist', 'Deep Learning', 'Neural networks, CNN, RNN, transformers, PyTorch/TensorFlow', 'ADVANCED', 'TECHNICAL', 35),
('Data Scientist', 'Data Engineering', 'ETL pipelines, data warehousing, big data technologies', 'INTERMEDIATE', 'TECHNICAL', 18),
('Data Scientist', 'Data Visualization', 'Storytelling with data, visualization tools, dashboard design', 'INTERMEDIATE', 'TECHNICAL', 12),

-- Product Manager
('Product Manager', 'Product Strategy', 'Market analysis, competitive positioning, product vision, roadmap planning', 'INTERMEDIATE', 'PRODUCT', 25),
('Product Manager', 'User Research', 'User interviews, surveys, usability testing, persona development', 'INTERMEDIATE', 'PRODUCT', 15),
('Product Manager', 'Data Analysis', 'Metrics, A/B testing, user behavior analysis, business intelligence', 'INTERMEDIATE', 'PRODUCT', 20),
('Product Manager', 'Stakeholder Management', 'Cross-functional collaboration, executive communication, conflict resolution', 'INTERMEDIATE', 'BEHAVIORAL', 18),
('Product Manager', 'Agile Product Development', 'Sprint planning, backlog management, user story writing, prioritization', 'INTERMEDIATE', 'PRODUCT', 12),

-- DevOps Engineer
('DevOps Engineer', 'CI/CD Pipelines', 'Jenkins, GitLab CI, GitHub Actions, deployment automation', 'INTERMEDIATE', 'TECHNICAL', 20),
('DevOps Engineer', 'Containerization', 'Docker, Kubernetes, container orchestration, microservices deployment', 'INTERMEDIATE', 'TECHNICAL', 25),
('DevOps Engineer', 'Infrastructure as Code', 'Terraform, CloudFormation, Ansible, infrastructure automation', 'INTERMEDIATE', 'TECHNICAL', 18),
('DevOps Engineer', 'Cloud Platforms', 'AWS, Azure, GCP, cloud services, cost optimization', 'INTERMEDIATE', 'TECHNICAL', 30),
('DevOps Engineer', 'Monitoring & Logging', 'Prometheus, Grafana, ELK stack, alerting, observability', 'INTERMEDIATE', 'TECHNICAL', 15),

-- Frontend Developer
('Frontend Developer', 'Modern JavaScript', 'ES6+, async/await, promises, modules, functional programming', 'INTERMEDIATE', 'TECHNICAL', 20),
('Frontend Developer', 'React Ecosystem', 'React hooks, state management, Redux, Next.js, performance optimization', 'INTERMEDIATE', 'TECHNICAL', 25),
('Frontend Developer', 'CSS & Styling', 'CSS Grid, Flexbox, preprocessors, design systems, responsive design', 'INTERMEDIATE', 'TECHNICAL', 15),
('Frontend Developer', 'Web Performance', 'Bundle optimization, lazy loading, caching, Core Web Vitals', 'INTERMEDIATE', 'TECHNICAL', 12),
('Frontend Developer', 'Testing', 'Jest, React Testing Library, E2E testing, component testing', 'INTERMEDIATE', 'TECHNICAL', 10),

-- Backend Developer
('Backend Developer', 'API Design', 'RESTful APIs, GraphQL, API documentation, versioning strategies', 'INTERMEDIATE', 'TECHNICAL', 18),
('Backend Developer', 'Database Design', 'Relational databases, NoSQL, ORM, query optimization, transactions', 'INTERMEDIATE', 'TECHNICAL', 20),
('Backend Developer', 'Microservices', 'Service decomposition, inter-service communication, API gateways', 'INTERMEDIATE', 'TECHNICAL', 25),
('Backend Developer', 'Security', 'Authentication, authorization, input validation, SQL injection prevention', 'INTERMEDIATE', 'TECHNICAL', 15),
('Backend Developer', 'Performance', 'Caching, database optimization, load balancing, horizontal scaling', 'INTERMEDIATE', 'TECHNICAL', 18); 