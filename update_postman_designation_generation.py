#!/usr/bin/env python3
"""
Script to update Postman collection with designation generation endpoints
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

    # Define the new designation generation endpoints
    designation_generation_endpoints = [
        {
            "name": "POST /api/v1/ai/designations/generate-for-departments",
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
                    "raw": "{{baseUrl}}/api/v1/ai/designations/generate-for-departments",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "ai", "designations", "generate-for-departments"]
                },
                "description": "Generate designations for all departments using GenAI"
            }
        },
        {
            "name": "POST /api/v1/ai/designations/generate-for-department/{departmentId}",
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
                    "raw": "{{baseUrl}}/api/v1/ai/designations/generate-for-department/1",
                    "host": ["{{baseUrl}}"],
                    "path": ["api", "v1", "ai", "designations", "generate-for-department", "1"]
                },
                "description": "Generate designations for a specific department using GenAI"
            }
        }
    ]

    # Add the new endpoints to the AIController
    if 'item' not in ai_controller:
        ai_controller['item'] = []
    
    # Add the new endpoints
    for endpoint in designation_generation_endpoints:
        ai_controller['item'].append(endpoint)

    # Update the collection info
    collection['info']['updated'] = datetime.now().isoformat()
    collection['info']['description'] = collection['info'].get('description', '') + '\n\nUpdated with designation generation endpoints'

    # Write the updated collection back to file
    try:
        with open('Tymbl.postman_collection.json', 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        print("✅ Postman collection updated successfully!")
        print(f"Added {len(designation_generation_endpoints)} designation generation endpoints:")
        for endpoint in designation_generation_endpoints:
            print(f"  - {endpoint['name']}")
        return True
    except Exception as e:
        print(f"Error writing updated collection: {e}")
        return False

if __name__ == "__main__":
    success = update_postman_collection()
    sys.exit(0 if success else 1) 