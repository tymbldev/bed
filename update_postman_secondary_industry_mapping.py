#!/usr/bin/env python3
"""
Script to update Postman collection with secondary industry mapping endpoints
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

    # Add new secondary industry mapping endpoints
    new_endpoints = [
        {
            "name": "Process All Secondary Industries",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    }
                ],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/secondary-industries/map-all",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "secondary-industries", "map-all"]
                },
                "description": "Extracts all unique secondary industries from companies table, uses Gemini AI to create standardized mappings, and stores them in a separate mapping table. Groups similar variations (e.g., 'Fortune 500', 'Fortune500', 'Fortune 500 Top') under the same parent category."
            },
            "response": []
        },
        {
            "name": "Process Single Secondary Industry",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    }
                ],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/secondary-industries/map-industry/Fortune 500",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "secondary-industries", "map-industry", "Fortune 500"],
                    "variable": [
                        {
                            "key": "industryName",
                            "value": "Fortune 500",
                            "description": "Secondary industry name to process and map"
                        }
                    ]
                },
                "description": "Uses Gemini AI to create a standardized mapping for a specific secondary industry. Groups similar variations under the same parent category."
            },
            "response": []
        },
        {
            "name": "Reset Secondary Industry Processed Flag",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "application/json"
                    }
                ],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/secondary-industries/reset-processed-flag",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "secondary-industries", "reset-processed-flag"]
                },
                "description": "Resets the processed flag to false for all secondary industry mappings, allowing reprocessing of industry mapping"
            },
            "response": []
        }
    ]

    # Add the new endpoints to the AIController
    if 'item' not in ai_controller:
        ai_controller['item'] = []
    
    # Add a separator section for secondary industry mapping
    separator_item = {
        "name": "--- SECONDARY INDUSTRY MAPPING ---",
        "request": {
            "method": "GET",
            "header": [],
            "url": {
                "raw": "{{base_url}}/api/v1/health",
                "host": ["{{base_url}}"],
                "path": ["api", "v1", "health"]
            },
            "description": "Section separator for Secondary Industry Mapping endpoints"
        },
        "response": []
    }
    
    ai_controller['item'].append(separator_item)
    ai_controller['item'].extend(new_endpoints)

    # Update the collection info
    collection['info']['updatedAt'] = datetime.now().isoformat()
    collection['info']['description'] = collection['info'].get('description', '') + '\n\nUpdated with Secondary Industry Mapping endpoints'

    # Write the updated collection back to file
    try:
        with open('Tymbl.postman_collection.json', 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        print("✅ Successfully updated Postman collection with Secondary Industry Mapping endpoints")
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