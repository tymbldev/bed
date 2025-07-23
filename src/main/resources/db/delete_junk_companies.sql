-- Script to delete junk-marked companies after manual review
-- WARNING: This will permanently delete companies marked as junk
-- Run this only after reviewing the junk-marked companies

-- First, let's see what companies are marked as junk
SELECT 
    id,
    name,
    junk_reason,
    parent_company_name,
    cleanup_processed_at
FROM companies 
WHERE is_junk = true
ORDER BY cleanup_processed_at DESC;

-- To delete all junk-marked companies, uncomment the following line:
-- DELETE FROM companies WHERE is_junk = true;

-- To delete specific junk companies by ID, uncomment and modify:
-- DELETE FROM companies WHERE id IN (1, 2, 3, 4, 5);

-- To delete junk companies with specific parent company, uncomment and modify:
-- DELETE FROM companies WHERE is_junk = true AND parent_company_name = 'Amazon Web Services';

-- To clear junk flags without deleting (undo junk marking), uncomment:
-- UPDATE companies SET is_junk = false, junk_reason = NULL, parent_company_name = NULL WHERE is_junk = true;

-- To get count of junk companies:
SELECT COUNT(*) as junk_companies_count FROM companies WHERE is_junk = true;

-- To get count of total companies:
SELECT COUNT(*) as total_companies_count FROM companies;

-- To get percentage of junk companies:
SELECT 
    ROUND(
        (COUNT(CASE WHEN is_junk = true THEN 1 END) * 100.0 / COUNT(*)), 
        2
    ) as junk_percentage 
FROM companies; 