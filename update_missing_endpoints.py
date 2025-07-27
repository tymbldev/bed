#!/usr/bin/env python3
import json

def add_missing_controllers():
    # Read current collection
    with open('Tymbl.postman_collection.json', 'r') as f:
        collection = json.load(f)
    
    # Add missing controllers
    missing_controllers = [
        {
            "name": "List of Companies",
            "item": [
                {
                    "name": "Search Companies",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/companies/search?keyword=google&page=0&size=10", "host": ["{{base_url}}"], "path": ["api", "v1", "companies", "search"], "query": [{"key": "keyword", "value": "google"}, {"key": "page", "value": "0"}, {"key": "size", "value": "10"}]}
                    }
                },
                {
                    "name": "Update Job Count",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"activeJobCount\":15}"},
                        "url": {"raw": "{{base_url}}/api/v1/companies/1/update-job-count", "host": ["{{base_url}}"], "path": ["api", "v1", "companies", "1", "update-job-count"]}
                    }
                }
            ]
        },
        {
            "name": "Utility Controller",
            "item": [
                {
                    "name": "Load Basic Company Data",
                    "request": {
                        "method": "POST",
                        "url": {"raw": "{{base_url}}/api/v1/utility/company-data/load-basic", "host": ["{{base_url}}"], "path": ["api", "v1", "utility", "company-data", "load-basic"]}
                    }
                },
                {
                    "name": "Update Detailed Company Data",
                    "request": {
                        "method": "POST",
                        "url": {"raw": "{{base_url}}/api/v1/utility/company-data/update-detailed", "host": ["{{base_url}}"], "path": ["api", "v1", "utility", "company-data", "update-detailed"]}
                    }
                },
                {
                    "name": "Cleanup Duplicate Companies",
                    "request": {
                        "method": "POST",
                        "url": {"raw": "{{base_url}}/api/v1/utility/company-data/cleanup-duplicates", "host": ["{{base_url}}"], "path": ["api", "v1", "utility", "company-data", "cleanup-duplicates"]}
                    }
                },
                {
                    "name": "Reindex Jobs",
                    "request": {
                        "method": "POST",
                        "url": {"raw": "{{base_url}}/api/v1/utility/job-admin/reindex", "host": ["{{base_url}}"], "path": ["api", "v1", "utility", "job-admin", "reindex"]}
                    }
                }
            ]
        },
        {
            "name": "Location Controller",
            "item": [
                {
                    "name": "Get All Countries",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/locations/countries", "host": ["{{base_url}}"], "path": ["api", "v1", "locations", "countries"]}
                    }
                },
                {
                    "name": "Get Cities by Country",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/locations/countries/1/cities", "host": ["{{base_url}}"], "path": ["api", "v1", "locations", "countries", "1", "cities"]}
                    }
                },
                {
                    "name": "Get All Cities",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/locations/cities", "host": ["{{base_url}}"], "path": ["api", "v1", "locations", "cities"]}
                    }
                },
                {
                    "name": "Get City by ID",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/locations/cities/1", "host": ["{{base_url}}"], "path": ["api", "v1", "locations", "cities", "1"]}
                    }
                },
                {
                    "name": "Search Cities",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/locations/cities/search?keyword=mountain", "host": ["{{base_url}}"], "path": ["api", "v1", "locations", "cities", "search"], "query": [{"key": "keyword", "value": "mountain"}]}
                    }
                }
            ]
        },
        {
            "name": "Institution Controller",
            "item": [
                {
                    "name": "Get All Institutions",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/institutions", "host": ["{{base_url}}"], "path": ["api", "v1", "institutions"]}
                    }
                },
                {
                    "name": "Create Institution",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"name\":\"Stanford University\",\"type\":\"UNIVERSITY\"}"},
                        "url": {"raw": "{{base_url}}/api/v1/institutions", "host": ["{{base_url}}"], "path": ["api", "v1", "institutions"]}
                    }
                },
                {
                    "name": "Get Institution by ID",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/institutions/1", "host": ["{{base_url}}"], "path": ["api", "v1", "institutions", "1"]}
                    }
                },
                {
                    "name": "Update Institution",
                    "request": {
                        "method": "PUT",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"name\":\"Updated University Name\"}"},
                        "url": {"raw": "{{base_url}}/api/v1/institutions/1", "host": ["{{base_url}}"], "path": ["api", "v1", "institutions", "1"]}
                    }
                },
                {
                    "name": "Delete Institution",
                    "request": {
                        "method": "DELETE",
                        "url": {"raw": "{{base_url}}/api/v1/institutions/1", "host": ["{{base_url}}"], "path": ["api", "v1", "institutions", "1"]}
                    }
                },
                {
                    "name": "Search Institutions",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/institutions/search?keyword=stanford", "host": ["{{base_url}}"], "path": ["api", "v1", "institutions", "search"], "query": [{"key": "keyword", "value": "stanford"}]}
                    }
                }
            ]
        },
        {
            "name": "Skill Controller",
            "item": [
                {
                    "name": "Get All Skills",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/skills", "host": ["{{base_url}}"], "path": ["api", "v1", "skills"]}
                    }
                }
            ]
        },
        {
            "name": "Notification Controller",
            "item": [
                {
                    "name": "Get Recent Notifications",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/notifications/recent/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "recent", "1"]}
                    }
                },
                {
                    "name": "Get All Notifications",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/notifications/all/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "all", "1"]}
                    }
                },
                {
                    "name": "Get Unread Notifications",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/notifications/unread/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "unread", "1"]}
                    }
                },
                {
                    "name": "Get Unread Count",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/notifications/unread-count/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "unread-count", "1"]}
                    }
                },
                {
                    "name": "Mark Notification as Read",
                    "request": {
                        "method": "PUT",
                        "url": {"raw": "{{base_url}}/api/v1/notifications/mark-read/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "mark-read", "1"]}
                    }
                },
                {
                    "name": "Mark All Notifications as Read",
                    "request": {
                        "method": "PUT",
                        "url": {"raw": "{{base_url}}/api/v1/notifications/mark-all-read/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "mark-all-read", "1"]}
                    }
                },
                {
                    "name": "Delete Notification",
                    "request": {
                        "method": "DELETE",
                        "url": {"raw": "{{base_url}}/api/v1/notifications/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "1"]}
                    }
                },
                {
                    "name": "Get Notifications by Type",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/notifications/type/1", "host": ["{{base_url}}"], "path": ["api", "v1", "notifications", "type", "1"]}
                    }
                }
            ]
        },
        {
            "name": "User Resume Controller",
            "item": [
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
                    "name": "Get User Resumes",
                    "request": {
                        "method": "GET",
                        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                        "url": {"raw": "{{base_url}}/api/v1/resumes/user", "host": ["{{base_url}}"], "path": ["api", "v1", "resumes", "user"]}
                    }
                },
                {
                    "name": "Get Latest Resume",
                    "request": {
                        "method": "GET",
                        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                        "url": {"raw": "{{base_url}}/api/v1/resumes/user/latest", "host": ["{{base_url}}"], "path": ["api", "v1", "resumes", "user", "latest"]}
                    }
                },
                {
                    "name": "Get Resume by ID",
                    "request": {
                        "method": "GET",
                        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                        "url": {"raw": "{{base_url}}/api/v1/resumes/1", "host": ["{{base_url}}"], "path": ["api", "v1", "resumes", "1"]}
                    }
                },
                {
                    "name": "Download Resume by UUID",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/resumes/download/abc123", "host": ["{{base_url}}"], "path": ["api", "v1", "resumes", "download", "abc123"]}
                    }
                },
                {
                    "name": "Download Resume by ID",
                    "request": {
                        "method": "GET",
                        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                        "url": {"raw": "{{base_url}}/api/v1/resumes/1/download", "host": ["{{base_url}}"], "path": ["api", "v1", "resumes", "1", "download"]}
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
                },
                {
                    "name": "Delete Resume",
                    "request": {
                        "method": "DELETE",
                        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                        "url": {"raw": "{{base_url}}/api/v1/resumes", "host": ["{{base_url}}"], "path": ["api", "v1", "resumes"]}
                    }
                }
            ]
        }
    ]
    
    # Add missing controllers to collection
    collection['item'].extend(missing_controllers)
    
    # Save updated collection
    with open('Tymbl.postman_collection.json', 'w') as f:
        json.dump(collection, f, indent=2)
    
    print("✅ Added missing controllers to Postman collection!")
    print("📊 Added controllers:")
    for controller in missing_controllers:
        print(f"   - {controller['name']} ({len(controller['item'])} endpoints)")
    
    # Count total endpoints
    total_endpoints = sum(len(section['item']) for section in collection['item'])
    print(f"🎯 Total endpoints now: {total_endpoints}")

if __name__ == "__main__":
    add_missing_controllers() 