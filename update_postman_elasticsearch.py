#!/usr/bin/env python3
"""
Postman Collection Update Script for Elasticsearch Indexing and Autosuggest Endpoints

This script adds the new Elasticsearch indexing endpoints to the AIController and 
the autosuggest endpoint to the DropdownController in the Postman collection.
"""

import json
import sys
from datetime import datetime

def add_elasticsearch_endpoints_to_postman(collection_file):
    """Add Elasticsearch indexing and autosuggest endpoints to Postman collection"""
    
    try:
        # Read the existing collection
        with open(collection_file, 'r', encoding='utf-8') as f:
            collection = json.load(f)
        
        # Find the AI Utilities folder
        ai_folder = None
        dropdowns_folder = None
        
        for item in collection['item']:
            if item.get('name') == 'AI & Utilities':
                ai_folder = item
            elif item.get('name') == 'Dropdowns & Common':
                dropdowns_folder = item
        
        if not ai_folder:
            print("❌ AI Utilities folder not found in collection")
            return False
        
        if not dropdowns_folder:
            print("❌ Dropdowns folder not found in collection")
            return False
        
        # Add Elasticsearch indexing endpoints to AI Utilities folder
        elasticsearch_endpoints = [
            {
                "name": "Index All Entities to Elasticsearch",
                "request": {
                    "method": "POST",
                    "header": [
                        {
                            "key": "Content-Type",
                            "value": "application/json"
                        }
                    ],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/elasticsearch/index-all",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "elasticsearch", "index-all"]
                    },
                    "description": "Indexes all companies, designations, and cities to separate Elasticsearch indexes for fast search and autosuggest functionality"
                }
            },
            {
                "name": "Index Companies to Elasticsearch",
                "request": {
                    "method": "POST",
                    "header": [
                        {
                            "key": "Content-Type",
                            "value": "application/json"
                        }
                    ],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/elasticsearch/index-companies",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "elasticsearch", "index-companies"]
                    },
                    "description": "Indexes all companies to the companies Elasticsearch index for fast search and autosuggest functionality"
                }
            },
            {
                "name": "Index Designations to Elasticsearch",
                "request": {
                    "method": "POST",
                    "header": [
                        {
                            "key": "Content-Type",
                            "value": "application/json"
                        }
                    ],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/elasticsearch/index-designations",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "elasticsearch", "index-designations"]
                    },
                    "description": "Indexes all designations to the designations Elasticsearch index for fast search and autosuggest functionality"
                }
            },
            {
                "name": "Index Cities to Elasticsearch",
                "request": {
                    "method": "POST",
                    "header": [
                        {
                            "key": "Content-Type",
                            "value": "application/json"
                        }
                    ],
                    "url": {
                        "raw": "{{base_url}}/api/v1/ai/elasticsearch/index-cities",
                        "host": ["{{base_url}}"],
                        "path": ["api", "v1", "ai", "elasticsearch", "index-cities"]
                    },
                    "description": "Indexes all cities to the cities Elasticsearch index for fast search and autosuggest functionality"
                }
            }
        ]
        
        # Add the endpoints to AI Utilities folder
        if 'item' not in ai_folder:
            ai_folder['item'] = []
        
        ai_folder['item'].extend(elasticsearch_endpoints)
        
        # Add Elasticsearch autosuggest endpoint to Dropdowns folder
        autosuggest_endpoint = {
            "name": "Elasticsearch Autosuggest",
            "request": {
                "method": "GET",
                "header": [],
                "url": {
                    "raw": "{{base_url}}/api/v1/dropdowns/autosuggest-elasticsearch?keyword={{keyword}}&entityType={{entityType}}&limit={{limit}}",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "dropdowns", "autosuggest-elasticsearch"],
                    "query": [
                        {
                            "key": "keyword",
                            "value": "{{keyword}}",
                            "description": "Search keyword (minimum 2 characters)"
                        },
                        {
                            "key": "entityType",
                            "value": "{{entityType}}",
                            "description": "Entity type: companies, designations, or cities"
                        },
                        {
                            "key": "limit",
                            "value": "{{limit}}",
                            "description": "Maximum number of results (default: 10)"
                        }
                    ]
                },
                "description": "Uses Elasticsearch to provide fast autosuggest functionality for companies, designations, and cities. Requires keyword (min 2 chars) and entity type (companies, designations, or cities)."
            }
        }
        
        # Add the endpoint to Dropdowns folder
        if 'item' not in dropdowns_folder:
            dropdowns_folder['item'] = []
        
        dropdowns_folder['item'].append(autosuggest_endpoint)
        
        # Add variables for the new endpoints
        if 'variable' not in collection:
            collection['variable'] = []
        
        # Add variables for autosuggest endpoint
        new_variables = [
            {
                "key": "keyword",
                "value": "google",
                "type": "string",
                "description": "Search keyword for autosuggest"
            },
            {
                "key": "entityType",
                "value": "companies",
                "type": "string",
                "description": "Entity type for autosuggest (companies, designations, cities)"
            },
            {
                "key": "limit",
                "value": "10",
                "type": "string",
                "description": "Maximum number of autosuggest results"
            }
        ]
        
        # Check if variables already exist
        existing_keys = [var['key'] for var in collection['variable']]
        for var in new_variables:
            if var['key'] not in existing_keys:
                collection['variable'].append(var)
        
        # Update collection info
        collection['info']['updatedAt'] = datetime.utcnow().isoformat() + 'Z'
        collection['info']['description'] = collection['info'].get('description', '') + '\n\nUpdated with Elasticsearch indexing and autosuggest endpoints.'
        
        # Write the updated collection back to file
        with open(collection_file, 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        
        print("✅ Successfully added Elasticsearch endpoints to Postman collection:")
        print("   📁 AI Utilities folder:")
        print("      - Index All Entities to Elasticsearch")
        print("      - Index Companies to Elasticsearch")
        print("      - Index Designations to Elasticsearch")
        print("      - Index Cities to Elasticsearch")
        print("   📁 Dropdowns folder:")
        print("      - Elasticsearch Autosuggest")
        print("   🔧 Added variables: keyword, entityType, limit")
        
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
    
    print("🚀 Updating Postman Collection with Elasticsearch Endpoints")
    print("=" * 60)
    
    success = add_elasticsearch_endpoints_to_postman(collection_file)
    
    if success:
        print("\n🎉 Postman collection updated successfully!")
        print("\n📋 New endpoints added:")
        print("   • POST /api/v1/ai/elasticsearch/index-all")
        print("   • POST /api/v1/ai/elasticsearch/index-companies")
        print("   • POST /api/v1/ai/elasticsearch/index-designations")
        print("   • POST /api/v1/ai/elasticsearch/index-cities")
        print("   • GET /api/v1/dropdowns/autosuggest-elasticsearch")
        print("\n💡 Usage:")
        print("   1. First run the indexing endpoints to populate Elasticsearch")
        print("   2. Then use the autosuggest endpoint with keyword and entityType parameters")
        print("   3. Set variables in Postman: keyword, entityType (companies/designations/cities), limit")
    else:
        print("\n❌ Failed to update Postman collection")
        sys.exit(1)

if __name__ == "__main__":
    main() 