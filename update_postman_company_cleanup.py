#!/usr/bin/env python3
"""
Script to add company cleanup endpoints to Postman collection
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

    # Define company cleanup endpoints
    cleanup_endpoints = [
        {
            "name": "Process All Companies for Cleanup",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/cleanup-all",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "cleanup-all"]
                },
                "description": "Process all unprocessed companies to identify and mark junk/product entries using GenAI (no deletion - only flagging)"
            },
            "response": []
        },
        {
            "name": "Process Specific Company for Cleanup",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/cleanup/AWS",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "cleanup", "AWS"],
                    "variable": [{"key": "companyName", "value": "AWS", "description": "Company name to process for cleanup"}]
                },
                "description": "Process a specific company to identify if it's a junk/product entry and mark it accordingly using GenAI"
            },
            "response": []
        },
        {
            "name": "Get All Junk-Marked Companies",
            "request": {
                "method": "GET",
                "header": [],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/cleanup/junk-marked",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "cleanup", "junk-marked"]
                },
                "description": "Retrieve all companies marked as junk for manual review before deletion"
            },
            "response": []
        },
        {
            "name": "Clear Junk Flag for Company",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/cleanup/clear-junk-flag/1",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "cleanup", "clear-junk-flag", "1"],
                    "variable": [{"key": "companyId", "value": "1", "description": "Company ID to clear junk flag"}]
                },
                "description": "Remove junk flag from a specific company, effectively undoing the junk marking"
            },
            "response": []
        },
        {
            "name": "Reset Company Cleanup Processed Flag",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "{{base_url}}/api/v1/ai/companies/cleanup/reset-processed-flag",
                    "host": ["{{base_url}}"],
                    "path": ["api", "v1", "ai", "companies", "cleanup", "reset-processed-flag"]
                },
                "description": "Reset the cleanup processed flag for all companies to allow reprocessing"
            },
            "response": []
        }
    ]

    # Add cleanup endpoints to existing items
    existing_items = ai_controller.get('item', [])
    existing_items.extend(cleanup_endpoints)
    ai_controller['item'] = existing_items

    # Update the collection info
    collection['info']['updatedAt'] = datetime.now().isoformat()
    collection['info']['description'] = collection['info'].get('description', '') + '\n\nAdded company cleanup endpoints'

    # Write the updated collection back to file
    try:
        with open('Tymbl.postman_collection.json', 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        print("✅ Successfully added company cleanup endpoints to Postman collection")
        print(f"📝 Added {len(cleanup_endpoints)} endpoints:")
        print("   - Process All Companies for Cleanup")
        print("   - Process Specific Company for Cleanup")
        print("   - Reset Company Cleanup Processed Flag")
        return True
    except Exception as e:
        print(f"Error writing updated Postman collection: {e}")
        return False

if __name__ == "__main__":
    success = update_postman_collection()
    sys.exit(0 if success else 1) 