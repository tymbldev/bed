-- Migration script to rename aboutUs and culture columns to aboutUsOriginal and cultureOriginal
-- This preserves the original crawled data while allowing AI-generated content in the original column names

-- Rename aboutUs column to aboutUsOriginal
ALTER TABLE companies CHANGE COLUMN about_us about_us_original TEXT;

-- Rename culture column to cultureOriginal  
ALTER TABLE companies CHANGE COLUMN culture culture_original TEXT;

-- Add new aboutUs column for AI-generated content
ALTER TABLE companies ADD COLUMN about_us TEXT AFTER about_us_original;

-- Add new culture column for AI-generated content
ALTER TABLE companies ADD COLUMN culture TEXT AFTER culture_original; 