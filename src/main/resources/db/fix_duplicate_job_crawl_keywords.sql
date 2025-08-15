-- Fix duplicate job crawl keywords manually
-- This script helps identify and fix duplicate entries that are causing the NonUniqueResultException

-- Step 1: Identify duplicates
SELECT 
    keyword, 
    portal_name, 
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(id ORDER BY id) as duplicate_ids,
    GROUP_CONCAT(created_at ORDER BY id) as created_dates
FROM job_crawl_keywords 
GROUP BY keyword, portal_name 
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, keyword, portal_name;

-- Step 2: View detailed information about duplicates
-- Replace 'YOUR_KEYWORD' and 'YOUR_PORTAL' with actual values from Step 1
SELECT 
    id,
    keyword,
    portal_name,
    portal_url,
    crawl_frequency_hours,
    is_active,
    last_crawled_date,
    created_at,
    updated_at
FROM job_crawl_keywords 
WHERE keyword = 'YOUR_KEYWORD' 
  AND portal_name = 'YOUR_PORTAL'
ORDER BY created_at;

-- Step 3: Keep the first (oldest) entry and delete others
-- Replace 'YOUR_KEYWORD' and 'YOUR_PORTAL' with actual values
-- Replace 'FIRST_ID' with the ID you want to keep

-- Option A: Delete specific duplicate IDs (recommended)
DELETE FROM job_crawl_keywords 
WHERE id IN (LIST_OF_DUPLICATE_IDS_TO_DELETE);

-- Option B: Delete all but the first occurrence
DELETE FROM job_crawl_keywords 
WHERE keyword = 'YOUR_KEYWORD' 
  AND portal_name = 'YOUR_PORTAL'
  AND id NOT IN (
    SELECT MIN(id) 
    FROM job_crawl_keywords 
    WHERE keyword = 'YOUR_KEYWORD' 
      AND portal_name = 'YOUR_PORTAL'
  );

-- Step 4: Verify duplicates are removed
SELECT 
    keyword, 
    portal_name, 
    COUNT(*) as count
FROM job_crawl_keywords 
GROUP BY keyword, portal_name 
HAVING COUNT(*) > 1;

-- Step 5: Add unique constraint to prevent future duplicates
-- Run this after all duplicates are removed
ALTER TABLE job_crawl_keywords 
ADD CONSTRAINT uk_keyword_portal UNIQUE (keyword, portal_name);

-- Example usage:
-- 1. Run Step 1 to see all duplicates
-- 2. For each duplicate group, run Step 2 to see details
-- 3. Decide which entry to keep and delete others using Step 3
-- 4. Run Step 4 to verify
-- 5. Run Step 5 to prevent future duplicates

-- Sample data cleanup (uncomment and modify as needed):
-- DELETE FROM job_crawl_keywords WHERE id IN (123, 124, 125);
-- DELETE FROM job_crawl_keywords WHERE id IN (456, 457);
