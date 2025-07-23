#!/usr/bin/env python3
"""
Script to update Postman collection with ALL AIController endpoints
"""

import json
import sys
from datetime import datetime

def update_postman_collection():
    # Read the current Postman collection
    try:
        with open('Tymbl.postman_collection.json', 'r', encoding='utf-8') as f:
            collection = json.load(f)
    except FileNotFoundError:
        print("Error: Tymbl.postman_collection.json not found")
        return False
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON in Postman collection: {e}")
        return False

    # Find the AIController section
    ai_controller = None
    for item in collection.get('item', []):
        if item.get('name') == 'AI & Utilities':
            for subitem in item.get('item', []):
                if subitem.get('name') == 'AIController':
                    ai_controller = subitem
                    break
            break

    if not ai_controller:
        print("Error: AIController section not found in Postman collection")
        return False

    # Define ALL AIController endpoints
    all_endpoints = [
        # Company Generation & Crawling
        {
            "name": "Generate Companies Batch",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/generate-batch",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "generate-batch"]
                },
                "description": "Generate and save companies industry-wise using Gemini"
            },
            "response": []
        },
        {
            "name": "Crawl All Companies",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/crawl",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "crawl"]
                },
                "description": "Crawl all companies from the companies.txt file"
            },
            "response": []
        },
        {
            "name": "Crawl Jobs for Company by ID",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/1/jobs/crawl",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "1", "jobs", "crawl"],
                    "variable": [{"key": "companyId", "value": "1", "description": "Company ID"}]
                },
                "description": "Crawl jobs for a specific company by ID"
            },
            "response": []
        },
        {
            "name": "Crawl Jobs for Company by Name",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/Google/jobs/crawl-by-name",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "Google", "jobs", "crawl-by-name"],
                    "variable": [{"key": "companyName", "value": "Google", "description": "Company name"}]
                },
                "description": "Crawl jobs for a company by name"
            },
            "response": []
        },
        
        # Industry Detection
        {
            "name": "Detect Industries for Companies",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/detect-industries",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "detect-industries"]
                },
                "description": "Detect industries for all unprocessed companies"
            },
            "response": []
        },
        {
            "name": "Reset Industry Processed Flag",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/detect-industries/reset",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "detect-industries", "reset"]
                },
                "description": "Reset industry processed flag for all companies"
            },
            "response": []
        },
        
        # Company Content Shortening
        {
            "name": "Shorten Company Content by ID",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/1/shorten-content",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "1", "shorten-content"],
                    "variable": [{"key": "companyId", "value": "1", "description": "Company ID"}]
                },
                "description": "Shorten company about us and culture content using AI"
            },
            "response": []
        },
        {
            "name": "Shorten All Companies Content",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/shorten-content-all",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "shorten-content-all"]
                },
                "description": "Shorten about us and culture content for all unprocessed companies"
            },
            "response": []
        },
        
        # Interview Questions
        {
            "name": "Generate Comprehensive Interview Questions",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/interview-questions/generate-comprehensive",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "interview-questions", "generate-comprehensive"]
                },
                "description": "Generate comprehensive interview questions for all skills"
            },
            "response": []
        },
        {
            "name": "Generate Interview Questions for Skill",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/interview-questions/generate-for-skill/Java",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "interview-questions", "generate-for-skill", "Java"],
                    "variable": [{"key": "skillName", "value": "Java", "description": "Skill name"}]
                },
                "description": "Generate comprehensive interview questions for specific skill"
            },
            "response": []
        },
        
        # Skills Generation
        {
            "name": "Generate and Save Tech Skills",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/skills/generate-and-save",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "skills", "generate-and-save"]
                },
                "description": "Generate and save more tech skills using AI"
            },
            "response": []
        },
        {
            "name": "Generate Topics for Skill",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/skills/Java/topics/generate-and-save",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "skills", "Java", "topics", "generate-and-save"],
                    "variable": [{"key": "skillName", "value": "Java", "description": "Skill name"}]
                },
                "description": "Generate and save topics for a technical skill"
            },
            "response": []
        },
        {
            "name": "Generate Topics for All Skills",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/skills/topics/generate-and-save-all",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "skills", "topics", "generate-and-save-all"]
                },
                "description": "Generate and save topics for all technical skills"
            },
            "response": []
        },
        {
            "name": "Generate Questions for Skill and Topic",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/skills/Java/topics/Collections/questions/generate-and-save",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "skills", "Java", "topics", "Collections", "questions", "generate-and-save"],
                    "variable": [
                        {"key": "skillName", "value": "Java", "description": "Skill name"},
                        {"key": "topicName", "value": "Collections", "description": "Topic name"}
                    ]
                },
                "description": "Generate and save interview questions for a skill and topic"
            },
            "response": []
        },
        {
            "name": "Generate Questions for All Topics of Skill",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/skills/Java/topics/questions/generate-and-save",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "skills", "Java", "topics", "questions", "generate-and-save"],
                    "variable": [{"key": "skillName", "value": "Java", "description": "Skill name"}]
                },
                "description": "Generate and save interview questions for all topics of a skill"
            },
            "response": []
        },
        {
            "name": "Generate Questions for All Skills and Topics",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/skills/topics/questions/generate-and-save-all",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "skills", "topics", "questions", "generate-and-save-all"]
                },
                "description": "Generate and save interview questions for all skills and topics"
            },
            "response": []
        },
        
        # Similar Content Generation
        {
            "name": "Generate Similar Designations",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/designations/generate-similar",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "designations", "generate-similar"]
                },
                "description": "Generate similar designations for all unprocessed designations"
            },
            "response": []
        },
        {
            "name": "Generate Similar Companies",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/generate-similar",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "generate-similar"]
                },
                "description": "Generate similar companies for all unprocessed companies"
            },
            "response": []
        },
        
        # URL Content Management
        {
            "name": "Get All URL Content",
            "request": {
                "method": "GET",
                "header": [],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/url-content",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "url-content"]
                },
                "description": "Get all URL content from the database"
            },
            "response": []
        },
        {
            "name": "Get URL Content by URL",
            "request": {
                "method": "GET",
                "header": [],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/url-content/https://example.com",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "url-content", "https://example.com"],
                    "variable": [{"key": "url", "value": "https://example.com", "description": "URL to get content for"}]
                },
                "description": "Get URL content for a specific URL"
            },
            "response": []
        },
        
        # City Generation
        {
            "name": "Generate Cities for Countries",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/cities/generate-for-countries",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "cities", "generate-for-countries"]
                },
                "description": "Generate cities for all unprocessed countries using GenAI"
            },
            "response": []
        },
        {
            "name": "Generate Cities for Country",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/cities/generate-for-country/1",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "cities", "generate-for-country", "1"],
                    "variable": [{"key": "countryId", "value": "1", "description": "Country ID"}]
                },
                "description": "Generate cities for a specific country using GenAI"
            },
            "response": []
        },
        {
            "name": "Reset Cities Processed Flag",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/cities/reset-processed-flag",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "cities", "reset-processed-flag"]
                },
                "description": "Reset cities processed flag for all countries"
            },
            "response": []
        },
        
        # Processed Names
        {
            "name": "Generate Processed Names for All Entities",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/processed-names/generate-all",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-all"]
                },
                "description": "Generate processed names for all unprocessed entities"
            },
            "response": []
        },
        {
            "name": "Generate Processed Names for Countries",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/processed-names/generate-countries",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-countries"]
                },
                "description": "Generate processed names for unprocessed countries"
            },
            "response": []
        },
        {
            "name": "Generate Processed Names for Cities",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/processed-names/generate-cities",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-cities"]
                },
                "description": "Generate processed names for unprocessed cities"
            },
            "response": []
        },
        {
            "name": "Generate Processed Names for Companies",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/processed-names/generate-companies",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-companies"]
                },
                "description": "Generate processed names for unprocessed companies"
            },
            "response": []
        },
        {
            "name": "Generate Processed Names for Designations",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/processed-names/generate-designations",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-designations"]
                },
                "description": "Generate processed names for unprocessed designations"
            },
            "response": []
        },
        {
            "name": "Reset Processed Name Generation Flag",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/processed-names/reset",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "processed-names", "reset"]
                },
                "description": "Reset processed name generation flag for all entities"
            },
            "response": []
        },
        
        # Designation Generation
        {
            "name": "Generate Designations for All Departments",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/designations/generate-for-departments",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "designations", "generate-for-departments"]
                },
                "description": "Generate designations for all departments using GenAI"
            },
            "response": []
        },
        {
            "name": "Generate Designations for Department",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/designations/generate-for-department/1",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "designations", "generate-for-department", "1"],
                    "variable": [{"key": "departmentId", "value": "1", "description": "Department ID"}]
                },
                "description": "Generate designations for a specific department using GenAI"
            },
            "response": []
        },
        
        # Company Shortnames
        {
            "name": "Generate Shortnames for All Companies",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/shortnames/generate-for-all",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "shortnames", "generate-for-all"]
                },
                "description": "Generate shortnames for all companies using GenAI"
            },
            "response": []
        },
        {
            "name": "Generate Shortname for Specific Company",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/shortnames/generate-for-company/Eternal",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "shortnames", "generate-for-company", "Eternal"],
                    "variable": [{"key": "companyName", "value": "Eternal", "description": "Company name"}]
                },
                "description": "Generate shortname for specific company using GenAI"
            },
            "response": []
        },
        
        # Secondary Industry Mapping
        {
            "name": "Process All Secondary Industries",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/secondary-industries/map-all",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "secondary-industries", "map-all"]
                },
                "description": "Process and map all secondary industries using GenAI"
            },
            "response": []
        },
        {
            "name": "Process Single Secondary Industry",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/secondary-industries/map-industry/Fortune 500",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "secondary-industries", "map-industry", "Fortune 500"],
                    "variable": [{"key": "industryName", "value": "Fortune 500", "description": "Secondary industry name"}]
                },
                "description": "Process and map a specific secondary industry using GenAI"
            },
            "response": []
        },
        {
            "name": "Reset Secondary Industry Processed Flag",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/secondary-industries/reset-processed-flag",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "secondary-industries", "reset-processed-flag"]
                },
                "description": "Reset processed flag for all secondary industry mappings"
            },
            "response": []
        }
    ]

    # Clear existing items and add all endpoints
    ai_controller['item'] = all_endpoints

    # Update the collection info
    collection['info']['updatedAt'] = datetime.now().isoformat()
    collection['info']['description'] = collection['info'].get('description', '') + '\n\nUpdated with ALL AIController endpoints'

    # Write the updated collection back to file
    try:
        with open('Tymbl.postman_collection.json', 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        print("✅ Successfully updated Postman collection with ALL AIController endpoints")
        print(f"📝 Added {len(all_endpoints)} endpoints:")
        print("   Categories:")
        print("   - Company Generation & Crawling (4 endpoints)")
        print("   - Industry Detection (2 endpoints)")
        print("   - Company Content Shortening (2 endpoints)")
        print("   - Interview Questions (2 endpoints)")
        print("   - Skills Generation (6 endpoints)")
        print("   - Similar Content Generation (2 endpoints)")
        print("   - URL Content Management (2 endpoints)")
        print("   - City Generation (3 endpoints)")
        print("   - Processed Names (6 endpoints)")
        print("   - Designation Generation (2 endpoints)")
        print("   - Company Shortnames (2 endpoints)")
        print("   - Secondary Industry Mapping (3 endpoints)")
        return True
    except Exception as e:
        print(f"Error writing updated Postman collection: {e}")
        return False

if __name__ == "__main__":
    success = update_postman_collection()
    sys.exit(0 if success else 1) 