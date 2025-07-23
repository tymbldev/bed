#!/usr/bin/env python3
"""
Script to update Postman collection with company shortname generation endpoints
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

    # Find the AIController section
    ai_controller = None
    for item in collection.get('item', []):
        if item.get('name') == 'AI & Utilities':
            for subitem in item.get('item', []):
                if subitem.get('name') == 'AIController':
                    ai_controller = subitem
                    break
            break

    if not ai_controller:
        print("Error: AIController section not found in Postman collection")
        return False

    # Add new company shortname endpoints
    new_endpoints = [
        {
            "name": "Generate Shortnames for All Companies",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    }
                ],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/shortnames/generate-for-all",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "shortnames", "generate-for-all"]
                },
                "description": "Uses Gemini AI to generate commonly used shortnames or nicknames for all companies in the database. Examples: 'Eternal' → 'Zomato', 'International Business Machines' → 'IBM', 'Microsoft' → 'MS'."
            },
            "response": []
        },
        {
            "name": "Generate Shortname for Specific Company",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    }
                ],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/shortnames/generate-for-company/Eternal",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "shortnames", "generate-for-company", "Eternal"],
                    "variable": [
                        {
                            "key": "companyName",
                            "value": "Eternal",
                            "description": "Company name to generate shortname for"
                        }
                    ]
                },
                "description": "Uses Gemini AI to generate the commonly used shortname or nickname for a specific company. Examples: 'Eternal' → 'Zomato', 'International Business Machines' → 'IBM'."
            },
            "response": []
        }
    ]

    # Add the new endpoints to the AIController
    if 'item' not in ai_controller:
        ai_controller['item'] = []
    
    # Add a separator section for company shortnames
    separator_item = {
        "name": "--- COMPANY SHORTNAME GENERATION ---",
        "request": {
            "method": "GET",
            "header": [],
            "url": {
                "raw": "{{base_url}}/api/v1/health",
                "host": ["{{base_url}}"],
                "path": ["api", "v1", "health"]
            },
            "description": "Section separator for Company Shortname Generation endpoints"
        },
        "response": []
    }
    
    ai_controller['item'].append(separator_item)
    ai_controller['item'].extend(new_endpoints)

    # Update the collection info
    collection['info']['updatedAt'] = datetime.now().isoformat()
    collection['info']['description'] = collection['info'].get('description', '') + '\n\nUpdated with Company Shortname Generation endpoints'

    # Write the updated collection back to file
    try:
        with open('Tymbl.postman_collection.json', 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        print("✅ Successfully updated Postman collection with Company Shortname Generation endpoints")
        print(f"📝 Added {len(new_endpoints)} new endpoints:")
        for endpoint in new_endpoints:
            print(f"   - {endpoint['name']}")
        return True
    except Exception as e:
        print(f"Error writing updated Postman collection: {e}")
        return False

if __name__ == "__main__":
    success = update_postman_collection()
    sys.exit(0 if success else 1) 