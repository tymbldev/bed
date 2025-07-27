#!/usr/bin/env python3
import json
import os

def create_complete_postman_collection():
    collection = {
        "info": {
            "name": "Tymbl API - Complete Collection",
            "description": "Complete Tymbl API Collection with all endpoints",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": [
            {
                "name": "Authentication",
                "item": [
                    {
                        "name": "Login",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"email\":\"user@example.com\",\"password\":\"password\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/auth/login", "host": ["{{base_url}}"], "path": ["api", "v1", "auth", "login"]}
                        }
                    },
                    {
                        "name": "Register",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"email\":\"user@example.com\",\"password\":\"password\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/auth/register", "host": ["{{base_url}}"], "path": ["api", "v1", "auth", "register"]}
                        }
                    },
                    {
                        "name": "LinkedIn Login",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"code\":\"linkedin_auth_code\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/auth/linkedin", "host": ["{{base_url}}"], "path": ["api", "v1", "auth", "linkedin"]}
                        }
                    }
                ]
            },
            {
                "name": "Job Management",
                "item": [
                    {
                        "name": "Create Job",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"title\":\"Software Engineer\",\"description\":\"Job description\",\"companyId\":1,\"designationId\":1,\"cityId\":1,\"countryId\":1,\"currencyId\":1,\"minSalary\":100000,\"maxSalary\":150000}"},
                            "url": {"raw": "{{base_url}}/api/v1/jobmanagement", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement"]}
                        }
                    },
                    {
                        "name": "Update Job",
                        "request": {
                            "method": "PUT",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"title\":\"Updated Job Title\",\"description\":\"Updated description\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/jobmanagement/1", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement", "1"]}
                        }
                    },
                    {
                        "name": "Delete Job",
                        "request": {
                            "method": "DELETE",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/jobmanagement/1", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement", "1"]}
                        }
                    },
                    {
                        "name": "Get My Jobs",
                        "request": {
                            "method": "GET",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/jobmanagement/my-posts?page=0&size=10", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement", "my-posts"], "query": [{"key": "page", "value": "0"}, {"key": "size", "value": "10"}]}
                        }
                    },
                    {
                        "name": "Get Job Details with Referrers",
                        "request": {
                            "method": "GET",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/jobmanagement/1/details", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement", "1", "details"]}
                        }
                    },
                    {
                        "name": "Register as Referrer",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"jobId\":1}"},
                            "url": {"raw": "{{base_url}}/api/v1/jobmanagement/register-referrer", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement", "register-referrer"]}
                        }
                    },
                    {
                        "name": "Accept Referral",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/jobmanagement/accept/1?status=SHORTLISTED", "host": ["{{base_url}}"], "path": ["api", "v1", "jobmanagement", "accept", "1"], "query": [{"key": "status", "value": "SHORTLISTED"}]}
                        }
                    }
                ]
            },
            {
                "name": "Job Search",
                "item": [
                    {
                        "name": "Get Job by ID",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/jobsearch/1", "host": ["{{base_url}}"], "path": ["api", "v1", "jobsearch", "1"]}
                        }
                    },
                    {
                        "name": "Search Jobs",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"keyword\":\"software engineer\",\"page\":0,\"size\":20}"},
                            "url": {"raw": "{{base_url}}/api/v1/jobsearch/search", "host": ["{{base_url}}"], "path": ["api", "v1", "jobsearch", "search"]}
                        }
                    },
                    {
                        "name": "Get Referrers for Job",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/jobsearch/1/referrers", "host": ["{{base_url}}"], "path": ["api", "v1", "jobsearch", "1", "referrers"]}
                        }
                    }
                ]
            },
            {
                "name": "Companies",
                "item": [
                    {
                        "name": "Create/Update Company",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"name\":\"Google\",\"description\":\"Technology company\",\"website\":\"https://google.com\",\"primaryIndustryId\":1}"},
                            "url": {"raw": "{{base_url}}/api/v1/companies", "host": ["{{base_url}}"], "path": ["api", "v1", "companies"]}
                        }
                    },
                    {
                        "name": "Get All Companies",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/companies?page=0&size=100", "host": ["{{base_url}}"], "path": ["api", "v1", "companies"], "query": [{"key": "page", "value": "0"}, {"key": "size", "value": "100"}]}
                        }
                    },
                    {
                        "name": "Get Company by ID",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/companies/1", "host": ["{{base_url}}"], "path": ["api", "v1", "companies", "1"]}
                        }
                    },
                    {
                        "name": "Get Industry-wise Companies",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/companies/industry-wise-companies", "host": ["{{base_url}}"], "path": ["api", "v1", "companies", "industry-wise-companies"]}
                        }
                    },
                    {
                        "name": "Get Companies by Industry",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/companies/by-industry/1?page=0&size=100", "host": ["{{base_url}}"], "path": ["api", "v1", "companies", "by-industry", "1"], "query": [{"key": "page", "value": "0"}, {"key": "size", "value": "100"}]}
                        }
                    }
                ]
            },
            {
                "name": "Job Applications",
                "item": [
                    {
                        "name": "Apply for Job",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"jobId\":1,\"jobReferrerId\":1}"},
                            "url": {"raw": "{{base_url}}/api/v1/my-applications", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications"]}
                        }
                    },
                    {
                        "name": "Get My Applications",
                        "request": {
                            "method": "GET",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/my-applications", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications"]}
                        }
                    },
                    {
                        "name": "Submit Referrer Feedback",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"jobId\":1,\"referrerUserId\":1,\"feedbackText\":\"Great experience\",\"score\":5}"},
                            "url": {"raw": "{{base_url}}/api/v1/my-applications/feedback", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications", "feedback"]}
                        }
                    },
                    {
                        "name": "Switch Referrer",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/my-applications/switch-referrer?applicationId=1&newJobReferrerId=2", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications", "switch-referrer"], "query": [{"key": "applicationId", "value": "1"}, {"key": "newJobReferrerId", "value": "2"}]}
                        }
                    }
                ]
            },
            {
                "name": "Job Applications Management",
                "item": [
                    {
                        "name": "Get Applications for My Jobs",
                        "request": {
                            "method": "GET",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/job-applications/my-jobs", "host": ["{{base_url}}"], "path": ["api", "v1", "job-applications", "my-jobs"]}
                        }
                    },
                    {
                        "name": "Get Applications for Job",
                        "request": {
                            "method": "GET",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/job-applications/job/1", "host": ["{{base_url}}"], "path": ["api", "v1", "job-applications", "job", "1"]}
                        }
                    },
                    {
                        "name": "Get Application Details",
                        "request": {
                            "method": "GET",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/job-applications/1", "host": ["{{base_url}}"], "path": ["api", "v1", "job-applications", "1"]}
                        }
                    }
                ]
            },
            {
                "name": "Super Admin",
                "item": [
                    {
                        "name": "Get Pending Jobs",
                        "request": {
                            "method": "GET",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/superadmin/jobs/pending?page=0&size=10", "host": ["{{base_url}}"], "path": ["api", "v1", "superadmin", "jobs", "pending"], "query": [{"key": "page", "value": "0"}, {"key": "size", "value": "10"}]}
                        }
                    },
                    {
                        "name": "Approve Job",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"status\":\"APPROVED\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/superadmin/jobs/1/approve", "host": ["{{base_url}}"], "path": ["api", "v1", "superadmin", "jobs", "1", "approve"]}
                        }
                    }
                ]
            },
            {
                "name": "AI Company Controller",
                "item": [
                    {
                        "name": "Generate Companies Batch",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"industries\":[\"Technology\",\"Finance\"]}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/companies/generate-batch", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "generate-batch"]}
                        }
                    },
                    {
                        "name": "Process Company Operations",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"companyName\":\"Google\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/companies/process", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "process"]}
                        }
                    },
                    {
                        "name": "Reset Company Flags",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"flags\":[\"is_crawled\",\"industry_processed\"],\"companyName\":\"Google\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/companies/reset-flags", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "reset-flags"]}
                        }
                    },
                    {
                        "name": "Generate Shortnames for All",
                        "request": {
                            "method": "POST",
                            "url": {"raw": "{{base_url}}/api/v1/ai/companies/shortnames/generate-for-all", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "shortnames", "generate-for-all"]}
                        }
                    },
                    {
                        "name": "Generate Shortname for Company",
                        "request": {
                            "method": "POST",
                            "url": {"raw": "{{base_url}}/api/v1/ai/companies/shortnames/generate-for-company/Google", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "shortnames", "generate-for-company", "Google"]}
                        }
                    }
                ]
            },
            {
                "name": "AI Dropdown Controller",
                "item": [
                    {
                        "name": "Generate Designations",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"industries\":[\"Technology\"]}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/dropdown/designations/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "dropdown", "designations", "generate"]}
                        }
                    },
                    {
                        "name": "Generate Similar Designations",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"designation\":\"Software Engineer\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/dropdown/designations/similar", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "dropdown", "designations", "similar"]}
                        }
                    },
                    {
                        "name": "Generate Cities",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"countries\":[\"United States\"]}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/dropdown/cities/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "dropdown", "cities", "generate"]}
                        }
                    },
                    {
                        "name": "Generate Industries",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"keywords\":[\"technology\",\"finance\"]}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/dropdown/industries/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "dropdown", "industries", "generate"]}
                        }
                    }
                ]
            },
            {
                "name": "AI Interview Controller",
                "item": [
                    {
                        "name": "Generate Interview Questions",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"designation\":\"Software Engineer\",\"company\":\"Google\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/interview/questions/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "interview", "questions", "generate"]}
                        }
                    },
                    {
                        "name": "Generate Skills",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"designation\":\"Software Engineer\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/interview/skills/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "interview", "skills", "generate"]}
                        }
                    }
                ]
            },
            {
                "name": "AI Controller",
                "item": [
                    {
                        "name": "Generate Similar Companies",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"companyName\":\"Google\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/similar-companies", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "similar-companies"]}
                        }
                    },
                    {
                        "name": "Process URL Content",
                        "request": {
                            "method": "POST",
                            "header": [{"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"url\":\"https://example.com\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/ai/url-content/process", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "url-content", "process"]}
                        }
                    }
                ]
            },
            {
                "name": "Dropdown Controller",
                "item": [
                    {
                        "name": "Get All Designations",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/dropdown/designations", "host": ["{{base_url}}"], "path": ["api", "v1", "dropdown", "designations"]}
                        }
                    },
                    {
                        "name": "Get All Cities",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/dropdown/cities", "host": ["{{base_url}}"], "path": ["api", "v1", "dropdown", "cities"]}
                        }
                    },
                    {
                        "name": "Get All Countries",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/dropdown/countries", "host": ["{{base_url}}"], "path": ["api", "v1", "dropdown", "countries"]}
                        }
                    },
                    {
                        "name": "Get All Industries",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/dropdown/industries", "host": ["{{base_url}}"], "path": ["api", "v1", "dropdown", "industries"]}
                        }
                    },
                    {
                        "name": "Get All Currencies",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/dropdown/currencies", "host": ["{{base_url}}"], "path": ["api", "v1", "dropdown", "currencies"]}
                        }
                    },
                    {
                        "name": "Get All Departments",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/dropdown/departments", "host": ["{{base_url}}"], "path": ["api", "v1", "dropdown", "departments"]}
                        }
                    },
                    {
                        "name": "Get All Companies",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/dropdown/companies", "host": ["{{base_url}}"], "path": ["api", "v1", "dropdown", "companies"]}
                        }
                    }
                ]
            },
            {
                "name": "Interview Controller",
                "item": [
                    {
                        "name": "Get Designations",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/interview/designations", "host": ["{{base_url}}"], "path": ["api", "v1", "interview", "designations"]}
                        }
                    },
                    {
                        "name": "Get Topics by Designation",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/interview/designations/Software%20Engineer/topics", "host": ["{{base_url}}"], "path": ["api", "v1", "interview", "designations", "Software Engineer", "topics"]}
                        }
                    },
                    {
                        "name": "Get Questions by Designation",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/interview/designations/Software%20Engineer/questions/general", "host": ["{{base_url}}"], "path": ["api", "v1", "interview", "designations", "Software Engineer", "questions", "general"]}
                        }
                    }
                ]
            },
            {
                "name": "User Controller",
                "item": [
                    {
                        "name": "Update Profile",
                        "request": {
                            "method": "PUT",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                            "body": {"mode": "raw", "raw": "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"designation\":\"Software Engineer\"}"},
                            "url": {"raw": "{{base_url}}/api/v1/users/profile", "host": ["{{base_url}}"], "path": ["api", "v1", "users", "profile"]}
                        }
                    },
                    {
                        "name": "Get Profile",
                        "request": {
                            "method": "GET",
                            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                            "url": {"raw": "{{base_url}}/api/v1/users/profile", "host": ["{{base_url}}"], "path": ["api", "v1", "users", "profile"]}
                        }
                    }
                ]
            },
            {
                "name": "Health Controller",
                "item": [
                    {
                        "name": "Health Check",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/health", "host": ["{{base_url}}"], "path": ["api", "v1", "health"]}
                        }
                    },
                    {
                        "name": "Database Health",
                        "request": {
                            "method": "GET",
                            "url": {"raw": "{{base_url}}/api/v1/health/database", "host": ["{{base_url}}"], "path": ["api", "v1", "health", "database"]}
                        }
                    }
                ]
            }
        ],
        "variable": [
            {"key": "base_url", "value": "http://localhost:8080", "type": "string"},
            {"key": "token", "value": "your_jwt_token_here", "type": "string"}
        ]
    }
    
    return collection

if __name__ == "__main__":
    # Remove existing collection
    if os.path.exists("Tymbl.postman_collection.json"):
        os.remove("Tymbl.postman_collection.json")
        print("🗑️ Removed existing Postman collection")
    
    # Create new collection
    collection = create_complete_postman_collection()
    
    with open("Tymbl.postman_collection.json", "w") as f:
        json.dump(collection, f, indent=2)
    
    print("✅ Generated complete Postman collection with ALL endpoints!")
    print("📊 Total sections: 12")
    print("🎯 Covers all controllers:")
    print("   - Authentication")
    print("   - Job Management") 
    print("   - Job Search")
    print("   - Companies")
    print("   - Job Applications")
    print("   - Job Applications Management")
    print("   - Super Admin")
    print("   - AI Company Controller")
    print("   - AI Dropdown Controller")
    print("   - AI Interview Controller")
    print("   - AI Controller")
    print("   - Dropdown Controller")
    print("   - Interview Controller")
    print("   - User Controller")
    print("   - Health Controller") 