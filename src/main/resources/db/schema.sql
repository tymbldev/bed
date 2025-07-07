-- Interview Preparation Tables
CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50),
    enabled BOOLEAN DEFAULT TRUE,
    usage_count BIGINT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Currency Table
CREATE TABLE IF NOT EXISTS currencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    exchange_rate DOUBLE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Institution Table
CREATE TABLE IF NOT EXISTS institutions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    location VARCHAR(255),
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Degree Table
CREATE TABLE IF NOT EXISTS degrees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    website VARCHAR(255),
    logo_url VARCHAR(255),
    about_us TEXT,
    vision TEXT,
    mission TEXT,
    values TEXT,
    culture TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Add new columns to companies table
ALTER TABLE companies 
ADD COLUMN IF NOT EXISTS linkedin_url VARCHAR(255),
ADD COLUMN IF NOT EXISTS headquarters VARCHAR(255),
ADD COLUMN IF NOT EXISTS industry VARCHAR(255),
ADD COLUMN IF NOT EXISTS company_size VARCHAR(255),
ADD COLUMN IF NOT EXISTS specialties TEXT,
ADD COLUMN IF NOT EXISTS is_crawled BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS last_crawled_at TIMESTAMP;

-- Organization Tables
CREATE TABLE IF NOT EXISTS designations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    level INT,
    enabled BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS company_designation_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    designation_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    importance_level VARCHAR(20),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (designation_id) REFERENCES designations(id),
    FOREIGN KEY (skill_id) REFERENCES skills(id)
);

CREATE TABLE IF NOT EXISTS interview_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_designation_skill_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    content TEXT,
    difficulty_level VARCHAR(20),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_designation_skill_id) REFERENCES company_designation_skills(id)
);

CREATE TABLE IF NOT EXISTS interview_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT,
    difficulty_level VARCHAR(20),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (topic_id) REFERENCES interview_topics(id)
);

CREATE TABLE IF NOT EXISTS company_interview_guides (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    section VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Location Tables
CREATE TABLE IF NOT EXISTS countries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(2) NOT NULL UNIQUE,
    phone_code VARCHAR(10),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country_id BIGINT NOT NULL,
    zip_code VARCHAR(20),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (country_id) REFERENCES countries(id)
);

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    zip_code VARCHAR(20),
    display_name VARCHAR(255) NOT NULL,
    city_id BIGINT,
    country_id BIGINT,
    is_remote BOOLEAN DEFAULT FALSE,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (city_id) REFERENCES cities(id),
    FOREIGN KEY (country_id) REFERENCES countries(id)
);

-- User Tables
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    designation_id BIGINT,
    designation VARCHAR(100),
    department_id BIGINT,
    city_id BIGINT,
    country_id BIGINT,
    phone_number VARCHAR(20),
    company VARCHAR(100),
    linkedin_profile VARCHAR(255),
    github_profile VARCHAR(255),
    portfolio_website VARCHAR(255),
    profile_picture VARCHAR(255),
    resume VARCHAR(255),
    profile_completion_percentage INT DEFAULT 0,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    years_of_experience INT,
    months_of_experience INT CHECK (months_of_experience >= 0 AND months_of_experience < 12),
    current_salary INT,
    current_salary_currency_id BIGINT,
    expected_salary INT,
    expected_salary_currency_id BIGINT,
    notice_period INT,
    zip_code VARCHAR(20),
    provider VARCHAR(20),
    provider_id VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_token_expiry BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (designation_id) REFERENCES designations(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (city_id) REFERENCES cities(id),
    FOREIGN KEY (country_id) REFERENCES countries(id),
    FOREIGN KEY (current_salary_currency_id) REFERENCES currencies(id),
    FOREIGN KEY (expected_salary_currency_id) REFERENCES currencies(id)
);

CREATE TABLE IF NOT EXISTS user_resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    resume_data BLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT valid_file_type CHECK (file_type IN ('application/pdf', 'text/plain', 'application/msword')),
    CONSTRAINT valid_file_size CHECK (file_size <= 5242880) -- 5MB in bytes
);

CREATE TABLE IF NOT EXISTS user_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_id BIGINT,
    skill_name VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (skill_id) REFERENCES skills(id)
);

CREATE TABLE IF NOT EXISTS user_education (
    user_id BIGINT NOT NULL,
    institution VARCHAR(255) NOT NULL,
    degree VARCHAR(100) NOT NULL,
    field_of_study VARCHAR(100),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    PRIMARY KEY (user_id, institution, degree),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Job Tables
CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    city_id BIGINT,
    country_id BIGINT,
    designation_id BIGINT,
    designation VARCHAR(255),
    salary DECIMAL(10,2) NOT NULL,
    currency_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    company VARCHAR(255),
    posted_by_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    opening_count INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    unique_url VARCHAR(1000) UNIQUE,
    platform VARCHAR(100),
    approved INT NOT NULL DEFAULT 0,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (city_id) REFERENCES cities(id),
    FOREIGN KEY (country_id) REFERENCES countries(id),
    FOREIGN KEY (designation_id) REFERENCES designations(id),
    FOREIGN KEY (currency_id) REFERENCES currencies(id)
);

CREATE TABLE IF NOT EXISTS job_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    cover_letter TEXT,
    resume_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    job_referrer_id BIGINT NOT NULL,
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    FOREIGN KEY (applicant_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS job_skills (
    job_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (job_id, skill_id),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id)
);

CREATE TABLE IF NOT EXISTS job_tags (
    job_id BIGINT NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (job_id, tag),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS job_referrers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (job_id, user_id),
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS referrer_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_referrer_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    feedback_text TEXT,
    got_call BOOLEAN,
    score INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (job_referrer_id, applicant_id),
    FOREIGN KEY (job_referrer_id) REFERENCES job_referrers(id),
    FOREIGN KEY (applicant_id) REFERENCES users(id)
);

