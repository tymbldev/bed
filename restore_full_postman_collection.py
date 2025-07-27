#!/usr/bin/env python3
"""
Restore full Postman collection with all endpoints plus dropdown-enriched examples
"""

import json
import os

def create_full_postman_collection():
    """Create a complete Postman collection with all endpoints"""
    
    collection = {
        "info": {
            "name": "Tymbl API",
            "description": "Complete Tymbl API Collection with Dropdown-Enriched Examples",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": [
            # Authentication
            {
                "name": "Authentication",
                "item": [
                    {
                        "name": "Login",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json",
                                    "type": "text"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "email": "user@example.com",
                                    "password": "password123"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/auth/login",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "auth", "login"]
                            }
                        }
                    },
                    {
                        "name": "Register",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json",
                                    "type": "text"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "firstName": "John",
                                    "lastName": "Doe",
                                    "email": "john@example.com",
                                    "password": "password123"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/auth/register",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "auth", "register"]
                            }
                        }
                    }
                ]
            },
            
            # Job Management
            {
                "name": "Job Management",
                "item": [
                    {
                        "name": "Create Job",
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
                                    "title": "Software Engineer",
                                    "description": "We are looking for a talented software engineer...",
                                    "cityId": 1,
                                    "countryId": 1,
                                    "designationId": 1,
                                    "minSalary": 100000,
                                    "maxSalary": 150000,
                                    "minExperience": 2,
                                    "maxExperience": 5,
                                    "jobType": "HYBRID",
                                    "currencyId": 1,
                                    "companyId": 1,
                                    "tags": ["Java", "Spring", "Microservices"],
                                    "openingCount": 5
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/jobmanagement",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "jobmanagement"]
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "body": json.dumps({
                                    "id": 1,
                                    "title": "Software Engineer",
                                    "description": "We are looking for a talented software engineer...",
                                    "cityId": 1,
                                    "cityName": "Mountain View",
                                    "countryId": 1,
                                    "countryName": "United States",
                                    "designationId": 1,
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
                                    "companyName": "Google",
                                    "tags": ["Java", "Spring", "Microservices"],
                                    "openingCount": 5,
                                    "active": True,
                                    "createdAt": "2024-01-01T10:00:00",
                                    "updatedAt": "2024-01-01T10:00:00"
                                }, indent=2)
                            }
                        ]
                    },
                    {
                        "name": "Get My Jobs",
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
                                "raw": "{{base_url}}/api/v1/jobmanagement/my-posts?page=0&size=10",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "jobmanagement", "my-posts"],
                                "query": [
                                    {
                                        "key": "page",
                                        "value": "0"
                                    },
                                    {
                                        "key": "size",
                                        "value": "10"
                                    }
                                ]
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "body": json.dumps({
                                    "content": [
                                        {
                                            "id": 1,
                                            "title": "Software Engineer",
                                            "cityId": 1,
                                            "cityName": "Mountain View",
                                            "countryId": 1,
                                            "countryName": "United States",
                                            "designationId": 1,
                                            "designationName": "Software Engineer",
                                            "currencyId": 1,
                                            "currencyName": "US Dollar",
                                            "currencySymbol": "$",
                                            "companyId": 1,
                                            "companyName": "Google"
                                        }
                                    ],
                                    "totalElements": 1,
                                    "totalPages": 1,
                                    "size": 10,
                                    "number": 0
                                }, indent=2)
                            }
                        ]
                    },
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
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
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
            
            # Job Search
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
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
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
                    },
                    {
                        "name": "Get Job by ID",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/jobsearch/1",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "jobsearch", "1"]
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "body": json.dumps({
                                    "id": 1,
                                    "title": "Software Engineer",
                                    "cityId": 1,
                                    "cityName": "Mountain View",
                                    "countryId": 1,
                                    "countryName": "United States",
                                    "designationId": 1,
                                    "designationName": "Software Engineer",
                                    "currencyId": 1,
                                    "currencyName": "US Dollar",
                                    "currencySymbol": "$",
                                    "companyId": 1,
                                    "companyName": "Google"
                                }, indent=2)
                            }
                        ]
                    }
                ]
            },
            
            # Companies
            {
                "name": "Companies",
                "item": [
                    {
                        "name": "Get All Companies",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/companies?page=0&size=100",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "companies"],
                                "query": [
                                    {
                                        "key": "page",
                                        "value": "0"
                                    },
                                    {
                                        "key": "size",
                                        "value": "100"
                                    }
                                ]
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "body": json.dumps({
                                    "content": [
                                        {
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
                                        }
                                    ],
                                    "totalElements": 1,
                                    "totalPages": 1,
                                    "size": 100,
                                    "number": 0
                                }, indent=2)
                            }
                        ]
                    },
                    {
                        "name": "Get Company by ID",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/companies/1",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "companies", "1"]
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
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
                    },
                    {
                        "name": "Get Industry-wise Companies",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/companies/industry-wise-companies",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "companies", "industry-wise-companies"]
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "body": json.dumps([
                                    {
                                        "industryId": 1,
                                        "industryName": "Information Technology & Services",
                                        "industryDescription": "Technology and IT services industry",
                                        "companyCount": 25,
                                        "topCompanies": [
                                            {
                                                "companyId": 1,
                                                "companyName": "Google",
                                                "logoUrl": "https://example.com/google-logo.png",
                                                "website": "https://google.com",
                                                "headquarters": "Mountain View, CA",
                                                "activeJobCount": 15
                                            }
                                        ]
                                    }
                                ], indent=2)
                            }
                        ]
                    }
                ]
            },
            
            # Job Applications
            {
                "name": "Job Applications",
                "item": [
                    {
                        "name": "Get Applications for My Jobs",
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
                                "raw": "{{base_url}}/api/v1/job-applications/my-jobs",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "job-applications", "my-jobs"]
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "body": json.dumps([
                                    {
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
                                    }
                                ], indent=2)
                            }
                        ]
                    }
                ]
            },
            
            # AI Company Controller
            {
                "name": "AI Company Controller",
                "item": [
                    {
                        "name": "Generate Companies Batch",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json",
                                    "type": "text"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "industries": ["Technology", "Finance", "Healthcare"]
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/ai/companies/generate-batch",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "ai", "companies", "generate-batch"]
                            }
                        }
                    },
                    {
                        "name": "Process Company Operations",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json",
                                    "type": "text"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "companyName": "Google"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/ai/companies/process",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "ai", "companies", "process"]
                            }
                        }
                    },
                    {
                        "name": "Reset Company Processing Flags",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json",
                                    "type": "text"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "flags": "is_crawled,industry_processed",
                                    "companyName": "Google"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/ai/companies/reset-flags",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "ai", "companies", "reset-flags"]
                            }
                        }
                    }
                ]
            },
            
            # AI Dropdown Controller
            {
                "name": "AI Dropdown Controller",
                "item": [
                    {
                        "name": "Generate Designations",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json",
                                    "type": "text"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "industry": "Technology"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/ai/dropdown/designations/generate",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "ai", "dropdown", "designations", "generate"]
                            }
                        }
                    },
                    {
                        "name": "Generate Cities",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json",
                                    "type": "text"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "country": "United States"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/ai/dropdown/cities/generate",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "ai", "dropdown", "cities", "generate"]
                            }
                        }
                    }
                ]
            },
            
            # AI Interview Controller
            {
                "name": "AI Interview Controller",
                "item": [
                    {
                        "name": "Generate Interview Questions",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json",
                                    "type": "text"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "designation": "Software Engineer",
                                    "company": "Google"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/ai/interview/questions/generate",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "ai", "interview", "questions", "generate"]
                            }
                        }
                    }
                ]
            },
            
            # Dropdown Controller
            {
                "name": "Dropdown Controller",
                "item": [
                    {
                        "name": "Get All Designations",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/dropdown/designations",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "dropdown", "designations"]
                            }
                        }
                    },
                    {
                        "name": "Get All Cities",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/dropdown/cities",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "dropdown", "cities"]
                            }
                        }
                    },
                    {
                        "name": "Get All Countries",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/dropdown/countries",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "dropdown", "countries"]
                            }
                        }
                    },
                    {
                        "name": "Get All Industries",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/dropdown/industries",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "dropdown", "industries"]
                            }
                        }
                    },
                    {
                        "name": "Get All Currencies",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/api/v1/dropdown/currencies",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "dropdown", "currencies"]
                            }
                        }
                    }
                ]
            },
            
            # My Job Applications
            {
                "name": "My Job Applications",
                "item": [
                    {
                        "name": "Get My Applications",
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
                                "raw": "{{base_url}}/api/v1/my-applications",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "my-applications"]
                            }
                        }
                    },
                    {
                        "name": "Apply for Referral",
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
                                    "jobId": 1,
                                    "jobReferrerId": 1
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/api/v1/my-applications",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "my-applications"]
                            }
                        }
                    }
                ]
            },
            
            # Super Admin
            {
                "name": "Super Admin",
                "item": [
                    {
                        "name": "Get Pending Jobs",
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
                                "raw": "{{base_url}}/api/v1/superadmin/jobs/pending?page=0&size=10",
                                "host": ["{{base_url}}"],
                                "path": ["api", "v1", "superadmin", "jobs", "pending"],
                                "query": [
                                    {
                                        "key": "page",
                                        "value": "0"
                                    },
                                    {
                                        "key": "size",
                                        "value": "10"
                                    }
                                ]
                            }
                        },
                        "response": [
                            {
                                "name": "Dropdown Enriched Response",
                                "status": "OK",
                                "code": 200,
                                "header": [
                                    {
                                        "key": "Content-Type",
                                        "value": "application/json"
                                    }
                                ],
                                "body": json.dumps({
                                    "content": [
                                        {
                                            "id": 1,
                                            "title": "Software Engineer",
                                            "cityId": 1,
                                            "cityName": "Mountain View",
                                            "countryId": 1,
                                            "countryName": "United States",
                                            "designationId": 1,
                                            "designationName": "Software Engineer",
                                            "currencyId": 1,
                                            "currencyName": "US Dollar",
                                            "currencySymbol": "$",
                                            "companyId": 1,
                                            "companyName": "Google"
                                        }
                                    ],
                                    "totalElements": 1,
                                    "totalPages": 1,
                                    "size": 10,
                                    "number": 0
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
    """Main function to restore full Postman collection"""
    
    # Create the collection
    collection = create_full_postman_collection()
    
    # Save to file
    with open('Tymbl.postman_collection.json', 'w') as f:
        json.dump(collection, f, indent=2)
    
    print("✅ Successfully restored full Postman collection")
    print("📝 Collection includes all original endpoints plus dropdown-enriched examples:")
    print("   - Authentication (Login, Register)")
    print("   - Job Management (Create, Get My Jobs, Get Details with Referrers)")
    print("   - Job Search (Search, Get by ID)")
    print("   - Companies (Get All, Get by ID, Industry-wise)")
    print("   - Job Applications (Get Applications for My Jobs)")
    print("   - AI Company Controller (Generate, Process, Reset Flags)")
    print("   - AI Dropdown Controller (Generate Designations, Cities)")
    print("   - AI Interview Controller (Generate Questions)")
    print("   - Dropdown Controller (All dropdown endpoints)")
    print("   - My Job Applications (Get, Apply)")
    print("   - Super Admin (Get Pending Jobs)")
    print("🎯 All GET endpoints include dropdown-enriched response examples!")

if __name__ == "__main__":
    main() 