#!/usr/bin/env python3
import json

# Read the current Postman collection
with open('Tymbl.postman_collection.json', 'r') as f:
    collection = json.load(f)

# Add company dropdown endpoints to the Dropdowns section
dropdowns_section = None
for item in collection['item']:
    if item['name'] == 'Dropdowns':
        dropdowns_section = item
        break

if dropdowns_section:
    # Add the new company endpoints
    company_dropdown_endpoint = {
        "name": "Get All Companies for Dropdown",
        "request": {
            "method": "GET",
            "header": [],
            "url": {
                "raw": "{{baseUrl}}/api/v1/dropdowns/companies",
                "host": ["{{baseUrl}}"],
                "path": ["api", "v1", "dropdowns", "companies"]
            },
            "description": "Get all companies with id and name for dropdown selection."
        },
        "response": [
            {
                "code": 200,
                "name": "List of companies retrieved successfully",
                "body": "[\n  {\n    \"id\": 1,\n    \"name\": \"Google\"\n  },\n  {\n    \"id\": 2,\n    \"name\": \"Microsoft\"\n  },\n  {\n    \"id\": 3,\n    \"name\": \"Amazon\"\n  }\n]"
            }
        ]
    }
    
    companies_map_endpoint = {
        "name": "Get Companies as Map",
        "request": {
            "method": "GET",
            "header": [],
            "url": {
                "raw": "{{baseUrl}}/api/v1/dropdowns/companies-map",
                "host": ["{{baseUrl}}"],
                "path": ["api", "v1", "dropdowns", "companies-map"]
            },
            "description": "Get companies as a map of value/label pairs for dropdown components."
        },
        "response": [
            {
                "code": 200,
                "name": "Companies map retrieved successfully",
                "body": "[\n  {\n    \"value\": \"1\",\n    \"label\": \"Google\"\n  },\n  {\n    \"value\": \"2\",\n    \"label\": \"Microsoft\"\n  },\n  {\n    \"value\": \"3\",\n    \"label\": \"Amazon\"\n  }\n]"
            }
        ]
    }
    
    # Insert the new endpoints after the first endpoint
    dropdowns_section['item'].insert(1, company_dropdown_endpoint)
    dropdowns_section['item'].insert(2, companies_map_endpoint)

# Add Company Crawler section
company_crawler_section = {
    "name": "Company Crawler",
    "description": "APIs for crawling company information and jobs from LinkedIn.",
    "item": [
        {
            "name": "Crawl All Companies",
            "request": {
                "method": "POST",
                "header": [],
                "url": {
                    "raw": "{{baseUrl}}/api/v1/crawler/companies/crawl",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "crawler", "companies", "crawl"]
                },
                "description": "Triggers the crawling process for all companies from the companies.txt file."
            },
            "response": [
                {
                    "code": 200,
                    "name": "Company crawling process completed successfully",
                    "body": "{\n  \"message\": \"Company crawling process completed successfully\",\n  \"status\": \"SUCCESS\"\n}"
                },
                {
                    "code": 500,
                    "name": "Internal server error during crawling process",
                    "body": "{\n  \"message\": \"Error during company crawling process: [error details]\",\n  \"status\": \"ERROR\"\n}"
                }
            ]
        },
        {
            "name": "Crawl Jobs for Company by ID",
            "request": {
                "method": "POST",
                "header": [],
                "url": {
                    "raw": "{{baseUrl}}/api/v1/crawler/companies/1/jobs/crawl",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "crawler", "companies", "1", "jobs", "crawl"]
                },
                "description": "Triggers job crawling for a specific company by ID."
            },
            "response": [
                {
                    "code": 200,
                    "name": "Job crawling process completed successfully",
                    "body": "{\n  \"message\": \"Job crawling process completed successfully for company: Google\",\n  \"companyId\": 1,\n  \"companyName\": \"Google\",\n  \"status\": \"SUCCESS\"\n}"
                },
                {
                    "code": 404,
                    "name": "Company not found",
                    "body": "{\n  \"error\": \"Company not found\"\n}"
                },
                {
                    "code": 500,
                    "name": "Internal server error during crawling process",
                    "body": "{\n  \"message\": \"Error during job crawling process: [error details]\",\n  \"companyId\": 1,\n  \"status\": \"ERROR\"\n}"
                }
            ]
        },
        {
            "name": "Crawl Jobs for Company by Name",
            "request": {
                "method": "POST",
                "header": [],
                "url": {
                    "raw": "{{baseUrl}}/api/v1/crawler/companies/Google/jobs/crawl-by-name",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "crawler", "companies", "Google", "jobs", "crawl-by-name"]
                },
                "description": "Triggers job crawling for a company by name."
            },
            "response": [
                {
                    "code": 200,
                    "name": "Job crawling process completed successfully",
                    "body": "{\n  \"message\": \"Job crawling process completed successfully for company: Google\",\n  \"companyName\": \"Google\",\n  \"status\": \"SUCCESS\"\n}"
                },
                {
                    "code": 500,
                    "name": "Internal server error during crawling process",
                    "body": "{\n  \"message\": \"Error during job crawling process: [error details]\",\n  \"companyName\": \"Google\",\n  \"status\": \"ERROR\"\n}"
                }
            ]
        }
    ]
}

# Insert the Company Crawler section before Company Management
company_management_index = None
for i, item in enumerate(collection['item']):
    if item['name'] == 'Company Management':
        company_management_index = i
        break

if company_management_index is not None:
    collection['item'].insert(company_management_index, company_crawler_section)

# Write the updated collection back to file
with open('Tymbl.postman_collection.json', 'w') as f:
    json.dump(collection, f, indent=2)

print("Postman collection updated successfully!") 