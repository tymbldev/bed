-- Migration script for comprehensive interview questions
-- This script updates the existing tables and creates new ones

-- 1. Update interview_questions table to be skill-based
ALTER TABLE interview_questions 
DROP FOREIGN KEY IF EXISTS interview_questions_ibfk_1;

ALTER TABLE interview_questions 
DROP COLUMN IF EXISTS topic_id;

ALTER TABLE interview_questions 
ADD COLUMN skill_id BIGINT NOT NULL AFTER id,
ADD COLUMN skill_name VARCHAR(255) NOT NULL AFTER skill_id,
ADD COLUMN summary_answer TEXT AFTER answer,
ADD COLUMN question_type VARCHAR(50) AFTER difficulty_level,
ADD COLUMN tags VARCHAR(500) AFTER question_type,
ADD COLUMN html_content LONGTEXT AFTER tags,
ADD COLUMN code_examples LONGTEXT AFTER html_content,
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER code_examples;

-- 2. Create designation_skill_question_mappings table
CREATE TABLE IF NOT EXISTS designation_skill_question_mappings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    designation_id BIGINT NOT NULL,
    designation_name VARCHAR(255) NOT NULL,
    skill_id BIGINT NOT NULL,
    skill_name VARCHAR(255) NOT NULL,
    question_id BIGINT NOT NULL,
    relevance_score DOUBLE DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_mapping (designation_id, skill_id, question_id),
    INDEX idx_designation_id (designation_id),
    INDEX idx_skill_id (skill_id),
    INDEX idx_question_id (question_id)
);

-- 3. Add indexes to interview_questions table
CREATE INDEX IF NOT EXISTS idx_skill_id ON interview_questions (skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_name ON interview_questions (skill_name);
CREATE INDEX IF NOT EXISTS idx_difficulty_level ON interview_questions (difficulty_level);
CREATE INDEX IF NOT EXISTS idx_question_type ON interview_questions (question_type);

-- 4. Update existing data if any (placeholder for data migration)
-- This would need to be customized based on existing data structure 