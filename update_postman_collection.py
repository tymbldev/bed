#!/usr/bin/env python3
"""
Comprehensive Postman Collection Update Script
Updates the Postman collection with all current controller endpoints
"""

import json
import os

def load_collection():
    """Load the existing Postman collection"""
    try:
        with open('Tymbl.postman_collection.json', 'r') as f:
            return json.load(f)
    except FileNotFoundError:
        print("Postman collection file not found. Creating new collection...")
        return create_new_collection()

def create_new_collection():
    """Create a new Postman collection structure"""
    return {
        "info": {
            "name": "Tymbl API Collection",
            "description": "Complete API collection for Tymbl job referral platform",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": []
    }

def add_job_admin_ops_section(collection):
    """Add Job Admin Operations section"""
    job_admin_section = {
        "name": "Job Admin Operations",
        "description": "Admin/on-demand operations for jobs, such as Elasticsearch reindexing.",
        "item": [
            {
                "name": "Reindex All Jobs to Elasticsearch",
                "request": {
                    "method": "POST",
                    "header": [],
                    "url": {
                        "raw": "{{baseUrl}}/api/v1/job-admin/reindex",
                        "host": ["{{baseUrl}}"],
                        "path": ["api", "v1", "job-admin", "reindex"]
                    },
                    "description": "Replaces all existing data in Elasticsearch with current job data from the database. This is an admin/on-demand operation."
                },
                "response": [
                    {
                        "code": 200,
                        "name": "Reindex completed successfully",
                        "body": "\"Reindex completed successfully\""
                    },
                    {
                        "code": 500,
                        "name": "Reindex failed",
                        "body": "\"Reindex failed: [error details]\""
                    }
                ]
            }
        ]
    }
    
    # Insert at the beginning of the collection
    collection['item'].insert(0, job_admin_section)

def update_company_crawler_section(collection):
    """Update Company Crawler section with the moved detect-industries endpoint"""
    # Find existing Company Crawler section
    crawler_section = None
    for item in collection['item']:
        if item.get('name') == 'Company Crawler':
            crawler_section = item
            break
    
    if not crawler_section:
        # Create new Company Crawler section if it doesn't exist
        crawler_section = {
            "name": "Company Crawler",
            "description": "APIs for crawling company information and jobs from LinkedIn.",
            "item": []
        }
        collection['item'].insert(1, crawler_section)  # Insert after Job Admin Operations
    
    # Add the detect-industries endpoint
    detect_industries_endpoint = {
        "name": "Detect Industries for All Companies",
        "request": {
            "method": "POST",
            "header": [],
            "url": {
                "raw": "{{baseUrl}}/api/v1/crawler/detect-industries?useGemini=false",
                "host": ["{{baseUrl}}"],
                "path": ["api", "v1", "crawler", "detect-industries"],
                "query": [
                    {
                        "key": "useGemini",
                        "value": "false",
                        "description": "Whether to use Gemini AI for industry detection"
                    }
                ]
            },
            "description": "Detects primary and secondary industries for all companies using AI or manual detection"
        },
        "response": [
            {
                "code": 200,
                "name": "Industry detection completed successfully",
                "body": "[\n  {\n    \"companyId\": 1,\n    \"companyName\": \"Yatra\",\n    \"primaryIndustry\": \"Travel & Hospitality Technology\",\n    \"primaryIndustryId\": 15,\n    \"secondaryIndustries\": [\"OTA\", \"Travel Tech\", \"Product Based Company\"],\n    \"processed\": true,\n    \"error\": null\n  },\n  {\n    \"companyId\": 2,\n    \"companyName\": \"Google\",\n    \"primaryIndustry\": \"Information Technology & Services\",\n    \"primaryIndustryId\": 1,\n    \"secondaryIndustries\": [\"Cloud Computing\", \"AI/ML\", \"Product Based Company\"],\n    \"processed\": true,\n    \"error\": null\n  }\n]"
            },
            {
                "code": 500,
                "name": "Internal server error",
                "body": "{\n  \"error\": \"Internal server error during industry detection\"\n}"
            }
        ]
    }
    
    # Add to existing items or create new list
    if 'item' not in crawler_section:
        crawler_section['item'] = []
    
    # Check if endpoint already exists
    existing_endpoints = [item.get('name') for item in crawler_section['item']]
    if 'Detect Industries for All Companies' not in existing_endpoints:
        crawler_section['item'].append(detect_industries_endpoint)

def remove_old_detect_industries_from_company_management(collection):
    """Remove the old detect-industries endpoint from Company Management section"""
    for section in collection['item']:
        if section.get('name') == 'Company Management':
            if 'item' in section:
                # Remove any existing detect-industries endpoint
                section['item'] = [item for item in section['item'] 
                                 if item.get('name') != 'Detect Industries for All Companies']

def update_job_search_section(collection):
    """Update Job Search section to remove the reindex endpoint (moved to Job Admin Operations)"""
    for section in collection['item']:
        if section.get('name') == 'Job Search':
            if 'item' in section:
                # Remove the reindex endpoint
                section['item'] = [item for item in section['item'] 
                                 if item.get('name') != 'Reindex All Jobs to Elasticsearch']

def add_missing_endpoints(collection):
    """Add any other missing endpoints that might be needed"""
    
    # Add AI Service endpoints if not present
    ai_service_section = None
    for item in collection['item']:
        if item.get('name') == 'AI Service':
            ai_service_section = item
            break
    
    if not ai_service_section:
        ai_service_section = {
            "name": "AI Service",
            "description": "AI-powered services for company and interview data generation",
            "item": [
                {
                    "name": "Generate Company Info",
                    "request": {
                        "method": "POST",
                        "header": [],
                        "url": {
                            "raw": "{{baseUrl}}/api/v1/ai/company/generate",
                            "host": ["{{baseUrl}}"],
                            "path": ["api", "v1", "ai", "company", "generate"]
                        },
                        "body": {
                            "mode": "raw",
                            "raw": "{\n  \"companyName\": \"Google\",\n  \"linkedinUrl\": \"https://www.linkedin.com/company/google\"\n}",
                            "options": {
                                "raw": {
                                    "language": "json"
                                }
                            }
                        },
                        "description": "Generate company information using AI"
                    },
                    "response": [
                        {
                            "code": 200,
                            "name": "Company info generated successfully",
                            "body": "{\n  \"id\": 1,\n  \"name\": \"Google\",\n  \"description\": \"Technology company\",\n  \"website\": \"https://google.com\",\n  \"headquarters\": \"Mountain View, CA\"\n}"
                        }
                    ]
                }
            ]
        }
        collection['item'].append(ai_service_section)

def main():
    """Main function to update the Postman collection"""
    print("Loading existing Postman collection...")
    collection = load_collection()
    
    print("Adding Job Admin Operations section...")
    add_job_admin_ops_section(collection)
    
    print("Updating Company Crawler section...")
    update_company_crawler_section(collection)
    
    print("Removing old detect-industries endpoint from Company Management...")
    remove_old_detect_industries_from_company_management(collection)
    
    print("Updating Job Search section...")
    update_job_search_section(collection)
    
    print("Adding missing endpoints...")
    add_missing_endpoints(collection)
    
    # Write the updated collection back to file
    print("Writing updated collection to file...")
    with open('Tymbl.postman_collection.json', 'w') as f:
        json.dump(collection, f, indent=2)
    
    print("Postman collection updated successfully!")
    print("\nSummary of changes:")
    print("- Added Job Admin Operations section with /api/v1/job-admin/reindex endpoint")
    print("- Moved detect-industries endpoint from Company Management to Company Crawler")
    print("- Removed reindex endpoint from Job Search (moved to Job Admin Operations)")
    print("- Added AI Service section for AI-powered endpoints")

if __name__ == "__main__":
    main() 