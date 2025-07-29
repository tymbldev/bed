#!/usr/bin/env python3
import json

def update_all_request_bodies():
    # Read current collection
    with open('Tymbl.postman_collection.json', 'r') as f:
        collection = json.load(f)
    
    # Define updates for each controller section
    updates = {
        "Authentication": [
            {
                "name": "Login",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"email\": \"user@example.com\",\n  \"password\": \"password\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/auth/login", "host": ["{{base_url}}"], "path": ["api", "v1", "auth", "login"]}
                }
            },
            {
                "name": "Register",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"email\": \"user@example.com\",\n  \"password\": \"password\",\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/auth/register", "host": ["{{base_url}}"], "path": ["api", "v1", "auth", "register"]}
                }
            },
            {
                "name": "LinkedIn Login",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"code\": \"linkedin_auth_code\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/auth/linkedin", "host": ["{{base_url}}"], "path": ["api", "v1", "auth", "linkedin"]}
                }
            }
        ],
        "Job Management": [
            {
                "name": "Create Job",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"title\": \"Software Engineer\",\n  \"description\": \"Job description\",\n  \"companyId\": 1,\n  \"designationId\": 1,\n  \"cityId\": 1,\n  \"countryId\": 1,\n  \"currencyId\": 1,\n  \"minSalary\": 100000,\n  \"maxSalary\": 150000\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/jobmanagement", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement"]}
                }
            },
            {
                "name": "Update Job",
                "request": {
                    "method": "PUT",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"title\": \"Updated Job Title\",\n  \"description\": \"Updated description\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/jobmanagement/1", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement", "1"]}
                }
            },
            {
                "name": "Register as Referrer",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"jobId\": 1\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/jobmanagement/register-referrer", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement", "register-referrer"]}
                }
            }
        ],
        "Job Search": [
            {
                "name": "Search Jobs",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"keyword\": \"software engineer\",\n  \"page\": 0,\n  \"size\": 20\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/jobsearch/search", "host": ["{{base_url}}"], "path": ["api", "v1", "jobsearch", "search"]}
                }
            }
        ],
        "Companies": [
            {
                "name": "Create/Update Company",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"name\": \"Google\",\n  \"description\": \"Technology company\",\n  \"website\": \"https://google.com\",\n  \"primaryIndustryId\": 1\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/companies", "host": ["{{base_url}}"], "path": ["api", "v1", "companies"]}
                }
            }
        ],
        "Job Applications": [
            {
                "name": "Apply for Job",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"jobId\": 1,\n  \"jobReferrerId\": 1\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/my-applications", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications"]}
                }
            },
            {
                "name": "Submit Referrer Feedback",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"jobId\": 1,\n  \"referrerUserId\": 1,\n  \"feedbackText\": \"Great experience\",\n  \"score\": 5\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/my-applications/feedback", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications", "feedback"]}
                }
            }
        ],
        "Super Admin": [
            {
                "name": "Approve Job",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"status\": \"APPROVED\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/superadmin/jobs/1/approve", "host": ["{{base_url}}"], "path": ["api", "v1", "superadmin", "jobs", "1", "approve"]}
                }
            }
        ],
        "AI Dropdown Controller": [
            {
                "name": "Generate Designations",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"industries\": [\"Technology\"]\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/ai/dropdown/designations/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "dropdown", "designations", "generate"]}
                }
            },
            {
                "name": "Generate Similar Designations",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"designation\": \"Software Engineer\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/ai/dropdown/designations/similar", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "dropdown", "designations", "similar"]}
                }
            },
            {
                "name": "Generate Cities",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"countries\": [\"United States\"]\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/ai/dropdown/cities/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "dropdown", "cities", "generate"]}
                }
            },
            {
                "name": "Generate Industries",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"keywords\": [\"technology\", \"finance\"]\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/ai/dropdown/industries/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "dropdown", "industries", "generate"]}
                }
            }
        ],
        "AI Interview Controller": [
            {
                "name": "Generate Interview Questions",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"designation\": \"Software Engineer\",\n  \"company\": \"Google\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/ai/interview/questions/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "interview", "questions", "generate"]}
                }
            },
            {
                "name": "Generate Skills",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"designation\": \"Software Engineer\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/ai/interview/skills/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "interview", "skills", "generate"]}
                }
            }
        ],
        "AI Controller": [
            {
                "name": "Generate Similar Companies",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"companyName\": \"Google\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/ai/similar-companies", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "similar-companies"]}
                }
            },
            {
                "name": "Process URL Content",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"url\": \"https://example.com\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/ai/url-content/process", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "url-content", "process"]}
                }
            }
        ],
        "List of Companies": [
            {
                "name": "Update Job Count",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"activeJobCount\": 15\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/companies/1/update-job-count", "host": ["{{base_url}}"], "path": ["api", "v1", "companies", "1", "update-job-count"]}
                }
            }
        ],
        "Utility Controller": [
            {
                "name": "Load Basic Company Data",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"note\": \"Loads basic company data from files\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/utility/company-data/load-basic", "host": ["{{base_url}}"], "path": ["api", "v1", "utility", "company-data", "load-basic"]}
                }
            },
            {
                "name": "Update Detailed Company Data",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"note\": \"Updates detailed company data\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/utility/company-data/update-detailed", "host": ["{{base_url}}"], "path": ["api", "v1", "utility", "company-data", "update-detailed"]}
                }
            },
            {
                "name": "Cleanup Duplicate Companies",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"note\": \"Cleans up duplicate companies\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/utility/company-data/cleanup-duplicates", "host": ["{{base_url}}"], "path": ["api", "v1", "utility", "company-data", "cleanup-duplicates"]}
                }
            },
            {
                "name": "Reindex Jobs",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"note\": \"Reindexes jobs in Elasticsearch\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/utility/job-admin/reindex", "host": ["{{base_url}}"], "path": ["api", "v1", "utility", "job-admin", "reindex"]}
                }
            }
        ],
        "Institution Controller": [
            {
                "name": "Create Institution",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"name\": \"Stanford University\",\n  \"type\": \"UNIVERSITY\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/institutions", "host": ["{{base_url}}"], "path": ["api", "v1", "institutions"]}
                }
            },
            {
                "name": "Update Institution",
                "request": {
                    "method": "PUT",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"name\": \"Updated University Name\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/institutions/1", "host": ["{{base_url}}"], "path": ["api", "v1", "institutions", "1"]}
                }
            }
        ],
        "Notification Controller": [
            {
                "name": "Mark Notification as Read",
                "request": {
                    "method": "PUT",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"note\": \"Marks notification as read\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/notifications/mark-read/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "mark-read", "1"]}
                }
            },
            {
                "name": "Mark All Notifications as Read",
                "request": {
                    "method": "PUT",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"note\": \"Marks all notifications as read\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/notifications/mark-all-read/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "mark-all-read", "1"]}
                }
            }
        ],
        "User Resume Controller": [
            {
                "name": "Upload Resume",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                    "body": {"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
                    "url": {"raw": "{{base_url}}/api/v1/resumes/upload", "host": ["{{base_url}}"], "path": ["api", "v1", "resumes", "upload"]}
                }
            },
            {
                "name": "Update Resume",
                "request": {
                    "method": "PUT",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                    "body": {"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
                    "url": {"raw": "{{base_url}}/api/v1/resumes/1", "host": ["{{base_url}}"], "path": ["api", "v1", "resumes", "1"]}
                }
            }
        ],
        "Registration Controller": [
            {
                "name": "Register User",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"email\": \"user@example.com\",\n  \"password\": \"password\",\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/registration", "host": ["{{base_url}}"], "path": ["api", "v1", "registration"]}
                }
            }
        ],
        "Interview Generation Controller": [
            {
                "name": "Generate Topics for Designation",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"topics\": [\"Data Structures\", \"Algorithms\"]\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/interview-generation/designations/Software%20Engineer/topics/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "designations", "Software Engineer", "topics", "generate"]}
                }
            },
            {
                "name": "Generate and Save Topics for Designation",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"topics\": [\"Data Structures\", \"Algorithms\"]\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/interview-generation/designations/Software%20Engineer/topics/generate-and-save", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "designations", "Software Engineer", "topics", "generate-and-save"]}
                }
            },
            {
                "name": "Generate Questions",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"topics\": [\"Data Structures\"],\n  \"designation\": \"Software Engineer\"\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/interview-generation/questions/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "questions", "generate"]}
                }
            },
            {
                "name": "Generate and Save Questions for Skill",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"topics\": [\"Java Programming\"]\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/interview-generation/skills/Java/topics/questions/generate-and-save", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "skills", "Java", "topics", "questions", "generate-and-save"]}
                }
            }
        ],
        "My Job Applications Controller": [
            {
                "name": "Apply for Job",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"jobId\": 1,\n  \"jobReferrerId\": 1\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/my-applications", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications"]}
                }
            },
            {
                "name": "Submit Referrer Feedback",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                    "body": {"mode": "raw", "raw": "{\n  \"jobId\": 1,\n  \"referrerUserId\": 1,\n  \"feedbackText\": \"Great experience\",\n  \"score\": 5\n}"},
                    "url": {"raw": "{{base_url}}/api/v1/my-applications/feedback", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications", "feedback"]}
                }
            }
        ]
    }
    
    # Update each section
    updated_count = 0
    for section_name, new_endpoints in updates.items():
        for section in collection['item']:
            if section['name'] == section_name:
                # Update existing endpoints that match by name
                for new_endpoint in new_endpoints:
                    for existing_endpoint in section['item']:
                        if existing_endpoint['name'] == new_endpoint['name']:
                            existing_endpoint['request'] = new_endpoint['request']
                            updated_count += 1
                            print(f"✅ Updated: {section_name} - {new_endpoint['name']}")
                break
    
    # Save updated collection
    with open('Tymbl.postman_collection.json', 'w') as f:
        json.dump(collection, f, indent=2)
    
    print(f"\n✅ Updated {updated_count} endpoints with proper request bodies!")
    
    # Count total endpoints
    total_endpoints = sum(len(section['item']) for section in collection['item'])
    print(f"🎯 Total endpoints in collection: {total_endpoints}")

if __name__ == "__main__":
    update_all_request_bodies() 