#!/usr/bin/env python3
"""
Update Postman collection with dropdown value examples for enriched responses
"""

import json
import os

def update_postman_dropdown_examples():
    """Update Postman collection with dropdown value examples"""
    
    # Read the current Postman collection
    collection_file = "Tymbl.postman_collection.json"
    
    if not os.path.exists(collection_file):
        print(f"❌ Postman collection file {collection_file} not found")
        return
    
    with open(collection_file, 'r') as f:
        collection = json.load(f)
    
    # Define dropdown-enriched response examples
    dropdown_examples = {
        # JobResponse examples with dropdown values
        "job_response_example": {
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
        },
        
        # CompanyResponse example with dropdown values
        "company_response_example": {
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
        },
        
        # JobDetailsWithReferrersResponse example
        "job_details_with_referrers_example": {
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
        },
        
        # JobApplicationResponseExtendedDetails example
        "job_application_extended_example": {
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
        },
        
        # JobSearchResponse example
        "job_search_response_example": {
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
        }
    }
    
    # Update collection items with dropdown examples
    updated_count = 0
    
    for item in collection.get('item', []):
        updated_count += update_item_examples(item, dropdown_examples)
    
    # Save updated collection
    with open(collection_file, 'w') as f:
        json.dump(collection, f, indent=2)
    
    print(f"✅ Successfully updated Postman collection with dropdown examples")
    print(f"📝 Updated {updated_count} endpoints with dropdown-enriched response examples")
    print(f"🎯 Added examples for:")
    print(f"   - JobResponse (with cityName, countryName, designationName, currencyName, currencySymbol, companyName)")
    print(f"   - CompanyResponse (with primaryIndustryName)")
    print(f"   - JobDetailsWithReferrersResponse (with all dropdown values)")
    print(f"   - JobApplicationResponseExtendedDetails (with comprehensive dropdown values)")
    print(f"   - JobSearchResponse (with dropdown-enriched job objects)")

def update_item_examples(item, dropdown_examples):
    """Update examples for a collection item"""
    updated_count = 0
    
    # Check if this is a folder
    if 'item' in item:
        for sub_item in item['item']:
            updated_count += update_item_examples(sub_item, dropdown_examples)
        return updated_count
    
    # Check if this is a request
    if 'request' in item:
        request = item['request']
        url = request.get('url', {})
        
        # Get the path
        if isinstance(url, dict):
            path = url.get('path', [])
        else:
            path = url.split('/') if isinstance(url, str) else []
        
        path_str = '/'.join(path) if isinstance(path, list) else str(path)
        
        # Update examples based on endpoint type
        if update_request_examples(request, path_str, dropdown_examples):
            updated_count += 1
    
    return updated_count

def update_request_examples(request, path_str, dropdown_examples):
    """Update examples for a specific request"""
    updated = False
    
    # Job-related endpoints
    if any(keyword in path_str.lower() for keyword in ['job', 'jobs']):
        if 'get' in request.get('method', '').lower():
            # Job search endpoints
            if 'search' in path_str.lower():
                update_response_example(request, dropdown_examples['job_search_response_example'])
                updated = True
            # Job details with referrers
            elif 'details' in path_str.lower():
                update_response_example(request, dropdown_examples['job_details_with_referrers_example'])
                updated = True
            # Regular job endpoints
            else:
                update_response_example(request, dropdown_examples['job_response_example'])
                updated = True
    
    # Company-related endpoints
    elif any(keyword in path_str.lower() for keyword in ['company', 'companies']):
        if 'get' in request.get('method', '').lower():
            update_response_example(request, dropdown_examples['company_response_example'])
            updated = True
    
    # Job application endpoints
    elif any(keyword in path_str.lower() for keyword in ['application', 'applications']):
        if 'get' in request.get('method', '').lower():
            update_response_example(request, dropdown_examples['job_application_extended_example'])
            updated = True
    
    return updated

def update_response_example(request, example_data):
    """Update response example for a request"""
    # Add or update response example
    if 'response' not in request:
        request['response'] = []
    
    # Find existing example or create new one
    example_found = False
    for response in request['response']:
        if response.get('name') == 'Dropdown Enriched Response':
            response['status'] = 'OK'
            response['code'] = 200
            response['header'] = [
                {
                    "key": "Content-Type",
                    "value": "application/json"
                }
            ]
            response['body'] = json.dumps(example_data, indent=2)
            example_found = True
            break
    
    if not example_found:
        request['response'].append({
            "name": "Dropdown Enriched Response",
            "status": "OK",
            "code": 200,
            "header": [
                {
                    "key": "Content-Type",
                    "value": "application/json"
                }
            ],
            "body": json.dumps(example_data, indent=2)
        })

if __name__ == "__main__":
    update_postman_dropdown_examples() 