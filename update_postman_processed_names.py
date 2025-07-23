#!/usr/bin/env python3
"""
Script to update Postman collection with processed name generation endpoints
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
            for sub_item in item.get('item', []):
                if sub_item.get('name') == 'AIController':
                    ai_controller = sub_item
                    break
            break

    if not ai_controller:
        print("Error: AIController section not found in Postman collection")
        return False

    # Define the new processed name endpoints
    processed_name_endpoints = [
        {
            "name": "POST /api/v1/ai/processed-names/generate-all",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    },
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authToken}}"
                    }
                ],
                "url": {
                    "raw": "{{baseUrl}}/api/v1/ai/processed-names/generate-all",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-all"]
                },
                "description": "Generate processed names for all unprocessed entities (countries, cities, companies, designations)"
            }
        },
        {
            "name": "POST /api/v1/ai/processed-names/generate-countries",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    },
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authToken}}"
                    }
                ],
                "url": {
                    "raw": "{{baseUrl}}/api/v1/ai/processed-names/generate-countries",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-countries"]
                },
                "description": "Generate processed names for unprocessed countries"
            }
        },
        {
            "name": "POST /api/v1/ai/processed-names/generate-cities",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    },
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authToken}}"
                    }
                ],
                "url": {
                    "raw": "{{baseUrl}}/api/v1/ai/processed-names/generate-cities",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-cities"]
                },
                "description": "Generate processed names for unprocessed cities"
            }
        },
        {
            "name": "POST /api/v1/ai/processed-names/generate-companies",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    },
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authToken}}"
                    }
                ],
                "url": {
                    "raw": "{{baseUrl}}/api/v1/ai/processed-names/generate-companies",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-companies"]
                },
                "description": "Generate processed names for unprocessed companies"
            }
        },
        {
            "name": "POST /api/v1/ai/processed-names/generate-designations",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    },
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authToken}}"
                    }
                ],
                "url": {
                    "raw": "{{baseUrl}}/api/v1/ai/processed-names/generate-designations",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "ai", "processed-names", "generate-designations"]
                },
                "description": "Generate processed names for unprocessed designations"
            }
        },
        {
            "name": "POST /api/v1/ai/processed-names/reset",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    },
                    {
                        "key": "Authorization",
                        "value": "Bearer {{authToken}}"
                    }
                ],
                "url": {
                    "raw": "{{baseUrl}}/api/v1/ai/processed-names/reset",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "ai", "processed-names", "reset"]
                },
                "description": "Reset processed name generation flag for all entities"
            }
        }
    ]

    # Add the new endpoints to the AIController
    if 'item' not in ai_controller:
        ai_controller['item'] = []
    
    # Add the new endpoints
    for endpoint in processed_name_endpoints:
        ai_controller['item'].append(endpoint)

    # Update the collection info
    collection['info']['updated'] = datetime.now().isoformat()
    collection['info']['description'] = collection['info'].get('description', '') + '\n\nUpdated with processed name generation endpoints'

    # Write the updated collection back to file
    try:
        with open('Tymbl.postman_collection.json', 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        print("✅ Postman collection updated successfully!")
        print(f"Added {len(processed_name_endpoints)} processed name generation endpoints:")
        for endpoint in processed_name_endpoints:
            print(f"  - {endpoint['name']}")
        return True
    except Exception as e:
        print(f"Error writing updated collection: {e}")
        return False

if __name__ == "__main__":
    success = update_postman_collection()
    sys.exit(0 if success else 1) 