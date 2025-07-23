#!/usr/bin/env python3
"""
Postman Collection Update Script for ListOfCompaniesController Endpoints

This script adds the new company search and job count update endpoints to the Postman collection.
"""

import json
import sys
from datetime import datetime

def add_list_companies_endpoints_to_postman(collection_file):
    """Add ListOfCompaniesController endpoints to Postman collection"""
    
    try:
        # Read the existing collection
        with open(collection_file, 'r', encoding='utf-8') as f:
            collection = json.load(f)
        
        # Find the Jobs & Companies folder
        jobs_companies_folder = None
        
        for item in collection['item']:
            if item.get('name') == 'Jobs & Companies':
                jobs_companies_folder = item
                break
        
        if not jobs_companies_folder:
            print("❌ Jobs & Companies folder not found in collection")
            return False
        
        # Add ListOfCompaniesController endpoints
        list_companies_endpoints = [
            {
                "name": "Search Companies with Filters",
                "request": {
                    "method": "GET",
                    "header": [],
                    "url": {
                        "raw": "{{base_url}}/api/v1/companies/search?location={{location}}&industryName={{industryName}}&secondaryIndustryName={{secondaryIndustryName}}&page={{page}}&size={{size}}",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "companies", "search"],
                        "query": [
                            {
                                "key": "location",
                                "value": "{{location}}",
                                "description": "Company headquarters location (optional)"
                            },
                            {
                                "key": "industryName",
                                "value": "{{industryName}}",
                                "description": "Primary industry name (optional)"
                            },
                            {
                                "key": "secondaryIndustryName",
                                "value": "{{secondaryIndustryName}}",
                                "description": "Secondary industry name (optional)"
                            },
                            {
                                "key": "page",
                                "value": "{{page}}",
                                "description": "Page number (default: 0)"
                            },
                            {
                                "key": "size",
                                "value": "{{size}}",
                                "description": "Page size (default: 10, max: 100)"
                            }
                        ]
                    },
                    "description": "Search companies by location (headquarters), industry name, and secondary industry name. Results are sorted by job count (descending) and then by company name (ascending)."
                }
            },
            {
                "name": "Update Company Job Count",
                "request": {
                    "method": "POST",
                    "header": [
                        {
                            "key": "Content-Type",
                            "value": "application/json"
                        }
                    ],
                    "url": {
                        "raw": "{{base_url}}/api/v1/companies/{{companyId}}/update-job-count",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "companies", "{{companyId}}", "update-job-count"]
                    },
                    "description": "Updates the job count for a specific company in Elasticsearch. This should be called when jobs are posted or updated."
                }
            }
        ]
        
        # Add the endpoints to Jobs & Companies folder
        if 'item' not in jobs_companies_folder:
            jobs_companies_folder['item'] = []
        
        jobs_companies_folder['item'].extend(list_companies_endpoints)
        
        # Add variables for the new endpoints
        if 'variable' not in collection:
            collection['variable'] = []
        
        # Add variables for company search endpoint
        new_variables = [
            {
                "key": "location",
                "value": "Mountain View",
                "type": "string",
                "description": "Company headquarters location for search"
            },
            {
                "key": "industryName",
                "value": "Information Technology",
                "type": "string",
                "description": "Primary industry name for search"
            },
            {
                "key": "secondaryIndustryName",
                "value": "Software",
                "type": "string",
                "description": "Secondary industry name for search"
            },
            {
                "key": "page",
                "value": "0",
                "type": "string",
                "description": "Page number for pagination"
            },
            {
                "key": "size",
                "value": "10",
                "type": "string",
                "description": "Page size for pagination"
            },
            {
                "key": "companyId",
                "value": "1",
                "type": "string",
                "description": "Company ID for job count update"
            }
        ]
        
        # Check if variables already exist
        existing_keys = [var['key'] for var in collection['variable']]
        for var in new_variables:
            if var['key'] not in existing_keys:
                collection['variable'].append(var)
        
        # Update collection info
        collection['info']['updatedAt'] = datetime.utcnow().isoformat() + 'Z'
        collection['info']['description'] = collection['info'].get('description', '') + '\n\nUpdated with ListOfCompaniesController endpoints.'
        
        # Write the updated collection back to file
        with open(collection_file, 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        
        print("✅ Successfully added ListOfCompaniesController endpoints to Postman collection:")
        print("   📁 Jobs & Companies folder:")
        print("      - Search Companies with Filters")
        print("      - Update Company Job Count")
        print("   🔧 Added variables: location, industryName, secondaryIndustryName, page, size, companyId")
        
        return True
        
    except FileNotFoundError:
        print(f"❌ Collection file not found: {collection_file}")
        return False
    except json.JSONDecodeError as e:
        print(f"❌ Invalid JSON in collection file: {e}")
        return False
    except Exception as e:
        print(f"❌ Error updating collection: {e}")
        return False

def main():
    """Main function"""
    collection_file = "Tymbl.postman_collection.json"
    
    print("🚀 Updating Postman Collection with ListOfCompaniesController Endpoints")
    print("=" * 70)
    
    success = add_list_companies_endpoints_to_postman(collection_file)
    
    if success:
        print("\n🎉 Postman collection updated successfully!")
        print("\n📋 New endpoints added:")
        print("   • GET /api/v1/companies/search")
        print("   • POST /api/v1/companies/{companyId}/update-job-count")
        print("\n💡 Usage:")
        print("   1. Use the search endpoint with optional filters: location, industryName, secondaryIndustryName")
        print("   2. Results are automatically sorted by job count (descending)")
        print("   3. Use the update endpoint to refresh job count when jobs are posted/updated")
        print("   4. Set variables in Postman for easy testing")
    else:
        print("\n❌ Failed to update Postman collection")
        sys.exit(1)

if __name__ == "__main__":
    main() 