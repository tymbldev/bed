#!/usr/bin/env python3
"""
Regenerate Postman collection with correct structure
"""

import json

def create_basic_postman_collection():
    """Create a basic Postman collection structure"""
    
    collection = {
        "info": {
            "name": "Tymbl API",
            "description": "Tymbl API Collection",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": [
            {
                "name": "Job Management",
                "item": [
                    {
                        "name": "Get Job Details with Referrers",
                        "request": {
                            "method": "GET",
                            "header": [
                                {
                                    "key": "Authorization",
                                    "value": "Bearer {{token}}",
                                    "type": "text"
                                }
                            ],
                            "url": {
                                "raw": "{{base_url}}/api/v1/jobmanagement/1/details",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "jobmanagement", "1", "details"]
                            },
                            "description": "Get job details with referrer profiles including dropdown values"
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "originalRequest": {
                                    "method": "GET",
                                    "header": [
                                        {
                                            "key": "Authorization",
                                            "value": "Bearer {{token}}",
                                            "type": "text"
                                        }
                                    ],
                                    "url": {
                                        "raw": "{{base_url}}/api/v1/jobmanagement/1/details",
                                        "host": ["{{base_url}}"],
                                        "path": ["api", "v1", "jobmanagement", "1", "details"]
                                    }
                                },
                                "status": "OK",
                                "code": 200,
                                "_postman_previewlanguage": "json",
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "cookie": [],
                                "body": json.dumps({
                                    "id": 1,
                                    "title": "Software Engineer",
                                    "description": "We are looking for a talented software engineer...",
                                    "cityId": 1,
                                    "cityName": "Mountain View",
                                    "countryId": 1,
                                    "countryName": "United States",
                                    "designationId": 1,
                                    "designation": "Software Engineer",
                                    "designationName": "Software Engineer",
                                    "minSalary": 100000,
                                    "maxSalary": 150000,
                                    "minExperience": 2,
                                    "maxExperience": 5,
                                    "jobType": "HYBRID",
                                    "currencyId": 1,
                                    "currencyName": "US Dollar",
                                    "currencySymbol": "$",
                                    "companyId": 1,
                                    "company": "Google",
                                    "companyName": "Google",
                                    "postedBy": 1,
                                    "active": True,
                                    "createdAt": "2024-01-01T10:00:00",
                                    "updatedAt": "2024-01-01T10:00:00",
                                    "tags": ["Java", "Spring", "Microservices"],
                                    "openingCount": 5,
                                    "uniqueUrl": "https://careers.google.com/jobs/123",
                                    "platform": "Google Careers",
                                    "approved": 1,
                                    "approvalStatus": "APPROVED",
                                    "referrerCount": 2,
                                    "referrers": [
                                        {
                                            "userId": 123,
                                            "userName": "Alice Smith",
                                            "email": "alice@google.com",
                                            "designation": "Senior Engineer",
                                            "company": "Google",
                                            "companyId": 1,
                                            "companyName": "Google",
                                            "yearsOfExperience": "5",
                                            "monthsOfExperience": "6",
                                            "education": "MS Computer Science from Stanford University",
                                            "portfolioWebsite": "https://alice.dev",
                                            "linkedInProfile": "https://linkedin.com/in/alice-smith",
                                            "githubProfile": "https://github.com/alice-smith",
                                            "numApplicationsAccepted": 5,
                                            "feedbackScore": 4.5,
                                            "overallScore": 7.2,
                                            "registeredAt": "2024-01-01T10:00:00"
                                        }
                                    ]
                                }, indent=2)
                            }
                        ]
                    }
                ]
            },
            {
                "name": "Job Search",
                "item": [
                    {
                        "name": "Search Jobs",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json",
                                    "type": "text"
                                },
                                {
                                    "key": "Authorization",
                                    "value": "Bearer {{token}}",
                                    "type": "text"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "keyword": "software engineer",
                                    "page": 0,
                                    "size": 20
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/jobsearch/search",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "jobsearch", "search"]
                            },
                            "description": "Search jobs with dropdown-enriched responses"
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "originalRequest": {
                                    "method": "POST",
                                    "header": [
                                        {
                                            "key": "Content-Type",
                                            "value": "application/json",
                                            "type": "text"
                                        },
                                        {
                                            "key": "Authorization",
                                            "value": "Bearer {{token}}",
                                            "type": "text"
                                        }
                                    ],
                                    "body": {
                                        "mode": "raw",
                                        "raw": json.dumps({
                                            "keyword": "software engineer",
                                            "page": 0,
                                            "size": 20
                                        }, indent=2)
                                    },
                                    "url": {
                                        "raw": "{{base_url}}/api/v1/jobsearch/search",
                                        "host": ["{{base_url}}"],
                                        "path": ["api", "v1", "jobsearch", "search"]
                                    }
                                },
                                "status": "OK",
                                "code": 200,
                                "_postman_previewlanguage": "json",
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "cookie": [],
                                "body": json.dumps({
                                    "jobs": [
                                        {
                                            "id": 1,
                                            "title": "Software Engineer",
                                            "description": "We are looking for a talented software engineer...",
                                            "company": "Google",
                                            "designation": "Software Engineer",
                                            "minSalary": 100000,
                                            "maxSalary": 150000,
                                            "minExperience": 2,
                                            "maxExperience": 5,
                                            "jobType": "HYBRID",
                                            "cityId": 1,
                                            "cityName": "Mountain View",
                                            "countryId": 1,
                                            "countryName": "United States",
                                            "companyId": 1,
                                            "companyName": "Google",
                                            "designationId": 1,
                                            "designationName": "Software Engineer",
                                            "currencyId": 1,
                                            "currencyName": "US Dollar",
                                            "currencySymbol": "$",
                                            "postedBy": 1,
                                            "active": True,
                                            "createdAt": "2024-01-01T10:00:00",
                                            "updatedAt": "2024-01-01T10:00:00",
                                            "tags": ["Java", "Spring", "Microservices"],
                                            "openingCount": 5,
                                            "uniqueUrl": "https://careers.google.com/jobs/123",
                                            "platform": "Google Careers",
                                            "approved": 1,
                                            "referrerCount": 2,
                                            "userRole": "VIEWER",
                                            "actualPostedBy": 1,
                                            "isSuperAdminPosted": False
                                        }
                                    ],
                                    "total": 1,
                                    "page": 0,
                                    "size": 20,
                                    "totalPages": 1,
                                    "companyMetaData": {
                                        "1": {
                                            "companyName": "Google",
                                            "logoUrl": "https://google.com/logo.png",
                                            "website": "https://google.com",
                                            "headquarters": "Mountain View, CA",
                                            "activeJobCount": 15,
                                            "secondaryIndustry": "Software,Cloud",
                                            "companySize": "100000+",
                                            "specialties": "AI,ML,Search",
                                            "careerPageUrl": "https://careers.google.com"
                                        }
                                    }
                                }, indent=2)
                            }
                        ]
                    }
                ]
            },
            {
                "name": "Companies",
                "item": [
                    {
                        "name": "Get Company by ID",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/companies/1",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "companies", "1"]
                            },
                            "description": "Get company details with dropdown values"
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "originalRequest": {
                                    "method": "GET",
                                    "header": [],
                                    "url": {
                                        "raw": "{{base_url}}/api/v1/companies/1",
                                        "host": ["{{base_url}}"],
                                        "path": ["api", "v1", "companies", "1"]
                                    }
                                },
                                "status": "OK",
                                "code": 200,
                                "_postman_previewlanguage": "json",
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "cookie": [],
                                "body": json.dumps({
                                    "id": 1,
                                    "name": "Google",
                                    "description": "A technology company",
                                    "website": "https://google.com",
                                    "logoUrl": "https://google.com/logo.png",
                                    "createdAt": "2024-01-01T10:00:00",
                                    "updatedAt": "2024-01-01T10:00:00",
                                    "aboutUs": "About Google",
                                    "vision": "To organize the world's information",
                                    "mission": "Make information universally accessible",
                                    "culture": "Innovative and inclusive",
                                    "jobs": [],
                                    "careerPageUrl": "https://careers.google.com",
                                    "linkedinUrl": "https://linkedin.com/company/google",
                                    "headquarters": "Mountain View, CA",
                                    "primaryIndustryId": 1,
                                    "primaryIndustryName": "Information Technology & Services",
                                    "secondaryIndustries": "Software,Cloud",
                                    "companySize": "100000+",
                                    "specialties": "AI,ML,Search"
                                }, indent=2)
                            }
                        ]
                    }
                ]
            },
            {
                "name": "Job Applications",
                "item": [
                    {
                        "name": "Get Job Application Details",
                        "request": {
                            "method": "GET",
                            "header": [
                                {
                                    "key": "Authorization",
                                    "value": "Bearer {{token}}",
                                    "type": "text"
                                }
                            ],
                            "url": {
                                "raw": "{{base_url}}/api/v1/job-applications/1",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "job-applications", "1"]
                            },
                            "description": "Get job application details with comprehensive dropdown values"
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "originalRequest": {
                                    "method": "GET",
                                    "header": [
                                        {
                                            "key": "Authorization",
                                            "value": "Bearer {{token}}",
                                            "type": "text"
                                        }
                                    ],
                                    "url": {
                                        "raw": "{{base_url}}/api/v1/job-applications/1",
                                        "host": ["{{base_url}}"],
                                        "path": ["api", "v1", "job-applications", "1"]
                                    }
                                },
                                "status": "OK",
                                "code": 200,
                                "_postman_previewlanguage": "json",
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "cookie": [],
                                "body": json.dumps({
                                    "id": 1,
                                    "jobId": 1,
                                    "jobTitle": "Software Engineer",
                                    "jobCityId": 1,
                                    "jobCityName": "Mountain View",
                                    "jobCountryId": 1,
                                    "jobCountryName": "United States",
                                    "jobDesignationId": 1,
                                    "jobDesignationName": "Software Engineer",
                                    "jobCurrencyId": 1,
                                    "jobCurrencyName": "US Dollar",
                                    "jobCurrencySymbol": "$",
                                    "jobCompanyId": 1,
                                    "jobCompanyName": "Google",
                                    "applicantId": 2,
                                    "applicantName": "John Doe",
                                    "applicantEmail": "john@example.com",
                                    "applicantCompanyId": 2,
                                    "applicantCompanyName": "Microsoft",
                                    "applicantDesignationId": 2,
                                    "applicantDesignationName": "Senior Developer",
                                    "applicantDepartmentId": 1,
                                    "applicantDepartmentNameValue": "Engineering",
                                    "applicantCityId": 2,
                                    "applicantCityNameValue": "Seattle",
                                    "applicantCountryId": 1,
                                    "applicantCountryNameValue": "United States",
                                    "applicantCurrentSalaryCurrencyId": 1,
                                    "applicantCurrentSalaryCurrencyName": "US Dollar",
                                    "applicantCurrentSalaryCurrencySymbol": "$",
                                    "applicantExpectedSalaryCurrencyId": 1,
                                    "applicantExpectedSalaryCurrencyName": "US Dollar",
                                    "applicantExpectedSalaryCurrencySymbol": "$",
                                    "status": "PENDING",
                                    "createdAt": "2024-01-01T10:00:00",
                                    "updatedAt": "2024-01-01T10:00:00"
                                }, indent=2)
                            }
                        ]
                    }
                ]
            }
        ],
        "variable": [
            {
                "key": "base_url",
                "value": "http://localhost:8080",
                "type": "string"
            },
            {
                "key": "token",
                "value": "your_jwt_token_here",
                "type": "string"
            }
        ]
    }
    
    return collection

def main():
    """Main function to regenerate Postman collection"""
    
    # Create the collection
    collection = create_basic_postman_collection()
    
    # Save to file
    with open('Tymbl.postman_collection.json', 'w') as f:
        json.dump(collection, f, indent=2)
    
    print("✅ Successfully regenerated Postman collection")
    print("📝 Collection includes dropdown-enriched response examples for:")
    print("   - Job Management endpoints")
    print("   - Job Search endpoints") 
    print("   - Company endpoints")
    print("   - Job Application endpoints")
    print("🎯 All examples include dropdown values (cityName, countryName, designationName, etc.)")

if __name__ == "__main__":
    main() 