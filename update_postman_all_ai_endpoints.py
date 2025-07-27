#!/usr/bin/env python3
"""
Script to update Postman collection with the new AI controller structure:
- AICompanyController: Company-related operations
- AIDropdownController: Dropdown-related operations  
- AIController: Remaining AI operations
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

    # Find the AI & Utilities section
    ai_utilities = None
    for item in collection.get('item', []):
        if item.get('name') == 'AI & Utilities':
            ai_utilities = item
            break

    if not ai_utilities:
        print("Error: AI & Utilities section not found in Postman collection")
        return False

    # Clear existing AI controllers and create new structure
    ai_utilities['item'] = []

    # ============================================================================
    # AICompanyController endpoints
    # ============================================================================
    ai_company_controller = {
        "name": "AICompanyController",
        "description": "Consolidated AI operations for company processing",
        "item": [
            # Company Generation
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
            
            # Company Processing (Consolidated)
            {
                "name": "Process Company Operations",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/companies/process?companyName=Google",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "companies", "process"],
                        "query": [{"key": "companyName", "value": "Google", "description": "Optional company name to process specific company"}]
                    },
                    "description": "Consolidated endpoint that processes all company operations. If companyName is provided, processes only that company. Otherwise processes all companies. Skips operations if their respective flags are already set."
                },
                "response": []
            },
            
            # Flag Reset
            {
                "name": "Reset Company Processing Flags",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/companies/reset-flags?flags=is_crawled,industry_processed&companyName=Google",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "companies", "reset-flags"],
                        "query": [
                            {"key": "flags", "value": "is_crawled,industry_processed", "description": "Comma-separated list of flags to reset: is_crawled, industry_processed, content_shortened, similar_companies_processed, cleanup_processed"},
                            {"key": "companyName", "value": "Google", "description": "Optional company name to reset flags for specific company"}
                        ]
                    },
                    "description": "Resets specific processing flags to allow reprocessing. If companyName is provided, resets flags for that specific company only."
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
            }
        ]
    }

    # ============================================================================
    # AIDropdownController endpoints
    # ============================================================================
    ai_dropdown_controller = {
        "name": "AIDropdownController",
        "description": "AI operations for dropdown-related data generation",
        "item": [
            # Designation Generation
            {
                "name": "Generate Similar Designations",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/designations/generate-similar",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "designations", "generate-similar"]
                    },
                    "description": "Generate similar designations for all existing designations"
                },
                "response": []
            },
            {
                "name": "Generate Designations for All Departments",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/designations/generate-for-departments",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "designations", "generate-for-departments"]
                    },
                    "description": "Generate designations for all departments"
                },
                "response": []
            },
            {
                "name": "Generate Designations for Specific Department",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/designations/generate-for-department/1",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "designations", "generate-for-department", "1"],
                        "variable": [{"key": "departmentId", "value": "1", "description": "Department ID"}]
                    },
                    "description": "Generate designations for a specific department"
                },
                "response": []
            },
            
            # City Generation
            {
                "name": "Generate Cities for All Countries",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/cities/generate-for-countries",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "cities", "generate-for-countries"]
                    },
                    "description": "Generate cities for all countries"
                },
                "response": []
            },
            {
                "name": "Generate Cities for Specific Country",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/cities/generate-for-country/1",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "cities", "generate-for-country", "1"],
                        "variable": [{"key": "countryId", "value": "1", "description": "Country ID"}]
                    },
                    "description": "Generate cities for a specific country"
                },
                "response": []
            },
            {
                "name": "Reset Cities Processed Flag",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/cities/reset-processed-flag",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "cities", "reset-processed-flag"]
                    },
                    "description": "Reset processed flag for all cities"
                },
                "response": []
            },
            
            # Processed Names
            {
                "name": "Generate Processed Names for Countries",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/processed-names/generate-countries",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "processed-names", "generate-countries"]
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
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/processed-names/generate-cities",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "processed-names", "generate-cities"]
                    },
                    "description": "Generate processed names for unprocessed cities"
                },
                "response": []
            },
            {
                "name": "Generate Processed Names for Designations",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/processed-names/generate-designations",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "processed-names", "generate-designations"]
                    },
                    "description": "Generate processed names for unprocessed designations"
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
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/secondary-industries/map-all",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "secondary-industries", "map-all"]
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
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/secondary-industries/map-industry/Fortune 500",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "secondary-industries", "map-industry", "Fortune 500"],
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
                        "raw": "{{base_url}}/api/v1/ai/dropdowns/secondary-industries/reset-processed-flag",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "dropdowns", "secondary-industries", "reset-processed-flag"]
                    },
                    "description": "Reset processed flag for all secondary industry mappings"
                },
                "response": []
            }
        ]
    }

    # ============================================================================
    # AIInterviewController endpoints
    # ============================================================================
    ai_interview_controller = {
        "name": "AIInterviewController",
        "description": "AI operations for interview-related data generation",
        "item": [
            # Interview Questions
            {
                "name": "Generate Comprehensive Interview Questions",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/interviews/interview-questions/generate-comprehensive",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "interviews", "interview-questions", "generate-comprehensive"]
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
                        "raw": "{{base_url}}/api/v1/ai/interviews/interview-questions/generate-for-skill/Java",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "interviews", "interview-questions", "generate-for-skill", "Java"],
                        "variable": [{"key": "skillName", "value": "Java", "description": "Skill name"}]
                    },
                    "description": "Generate interview questions for a specific skill"
                },
                "response": []
            },
            
            # Skills Generation
            {
                "name": "Generate and Save Skills",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/interviews/skills/generate-and-save",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "interviews", "skills", "generate-and-save"]
                    },
                    "description": "Generate and save skills using GenAI"
                },
                "response": []
            },
            {
                "name": "Generate Topics for Skill",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/interviews/skills/Java/topics/generate-and-save",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "interviews", "skills", "Java", "topics", "generate-and-save"],
                        "variable": [{"key": "skillName", "value": "Java", "description": "Skill name"}]
                    },
                    "description": "Generate topics for a specific skill"
                },
                "response": []
            },
            {
                "name": "Generate Topics for All Skills",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/interviews/skills/topics/generate-and-save-all",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "interviews", "skills", "topics", "generate-and-save-all"]
                    },
                    "description": "Generate topics for all skills"
                },
                "response": []
            },
            {
                "name": "Generate Questions for Skill Topic",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/interviews/skills/Java/topics/Collections/questions/generate-and-save",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "interviews", "skills", "Java", "topics", "Collections", "questions", "generate-and-save"],
                        "variable": [
                            {"key": "skillName", "value": "Java", "description": "Skill name"},
                            {"key": "topicName", "value": "Collections", "description": "Topic name"}
                        ]
                    },
                    "description": "Generate questions for a specific skill topic"
                },
                "response": []
            },
            {
                "name": "Generate Questions for All Skill Topics",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/interviews/skills/Java/topics/questions/generate-and-save",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "interviews", "skills", "Java", "topics", "questions", "generate-and-save"],
                        "variable": [{"key": "skillName", "value": "Java", "description": "Skill name"}]
                    },
                    "description": "Generate questions for all topics of a specific skill"
                },
                "response": []
            },
            {
                "name": "Generate Questions for All Skills Topics",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/interviews/skills/topics/questions/generate-and-save-all",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "interviews", "skills", "topics", "questions", "generate-and-save-all"]
                    },
                    "description": "Generate questions for all skills and topics"
                },
                "response": []
            }
        ]
    }

    # ============================================================================
    # AIController endpoints (remaining operations)
    # ============================================================================
    ai_controller = {
        "name": "AIController",
        "description": "Remaining AI operations for utilities and content management",
        "item": [
            # Similar Companies
            {
                "name": "Generate Similar Companies for All",
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
                    "header": [{"key": "Content-Type", "value": "application/json"}],
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
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/url-content/https%3A%2F%2Fexample.com",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "url-content", "https://example.com"],
                        "variable": [{"key": "url", "value": "https://example.com", "description": "URL to get content for"}]
                    },
                    "description": "Get URL content for a specific URL"
                },
                "response": []
            },
            
            # Processed Names (Companies)
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
                "name": "Remove Duplicate Processed Names",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/processed-names/remove-duplicates",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "processed-names", "remove-duplicates"]
                    },
                    "description": "Remove duplicate processed names from database"
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
            }
        ]
    }

    # Add all controllers to the AI & Utilities section
    ai_utilities['item'] = [
        ai_company_controller,
        ai_dropdown_controller,
        ai_interview_controller,
        ai_controller
    ]

    # Update the collection info
    collection['info']['updatedAt'] = datetime.now().isoformat()
    collection['info']['description'] = collection['info'].get('description', '') + '\n\nUpdated with new AI controller structure: AICompanyController, AIDropdownController, AIInterviewController, and updated AIController'

    # Write the updated collection back to file
    try:
        with open('Tymbl.postman_collection.json', 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        
        print("✅ Successfully updated Postman collection with new AI controller structure")
        print(f"📝 Updated structure:")
        print("   🏢 AICompanyController (5 endpoints):")
        print("      - Generate Companies Batch")
        print("      - Process Company Operations (consolidated)")
        print("      - Reset Company Processing Flags")
        print("      - Generate Shortnames for All Companies")
        print("      - Generate Shortname for Specific Company")
        print("   📋 AIDropdownController (12 endpoints):")
        print("      - Designation Generation (3 endpoints)")
        print("      - City Generation (3 endpoints)")
        print("      - Processed Names (3 endpoints)")
        print("      - Secondary Industry Mapping (3 endpoints)")
        print("   🎯 AIInterviewController (8 endpoints):")
        print("      - Interview Questions (2 endpoints)")
        print("      - Skills Generation (6 endpoints)")
        print("   🔧 AIController (7 endpoints):")
        print("      - Similar Companies (1 endpoint)")
        print("      - URL Content Management (2 endpoints)")
        print("      - Processed Names (4 endpoints)")
        print("   📊 Total: 32 endpoints across 4 controllers")
        return True
    except Exception as e:
        print(f"Error writing updated Postman collection: {e}")
        return False

if __name__ == "__main__":
    success = update_postman_collection()
    sys.exit(0 if success else 1) 