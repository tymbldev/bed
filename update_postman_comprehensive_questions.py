#!/usr/bin/env python3
"""
Script to update Postman collection with comprehensive interview question endpoints
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

    # Add the new comprehensive interview question endpoints
    new_endpoints = [
        {
            "name": "POST /api/v1/ai/interview-questions/generate-comprehensive",
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
                    "raw": "{{baseUrl}}/api/v1/ai/interview-questions/generate-comprehensive",
                    "host": ["{{baseUrl}}"],
                    "path": [
                        "api",
                        "v1",
                        "ai",
                        "interview-questions",
                        "generate-comprehensive"
                    ]
                },
                "description": "Generate comprehensive interview questions for all skills in the system using AI"
            },
            "response": [
                {
                    "name": "Success Response",
                    "originalRequest": {
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
                            "raw": "{{baseUrl}}/api/v1/ai/interview-questions/generate-comprehensive",
                            "host": ["{{baseUrl}}"],
                            "path": [
                                "api",
                                "v1",
                                "ai",
                                "interview-questions",
                                "generate-comprehensive"
                            ]
                        }
                    },
                    "status": "OK",
                    "code": 200,
                    "_postman_previewlanguage": "json",
                    "header": [
                        {
                            "key": "Content-Type",
                            "value": "application/json"
                        }
                    ],
                    "cookie": [],
                    "body": "{\n  \"total_skills_processed\": 15,\n  \"total_questions_generated\": 450,\n  \"message\": \"Comprehensive question generation completed\",\n  \"skill_results\": [\n    {\n      \"skill_name\": \"Java\",\n      \"skill_id\": 1,\n      \"questions_generated\": 30,\n      \"mappings_created\": 90,\n      \"status\": \"success\"\n    },\n    {\n      \"skill_name\": \"Python\",\n      \"skill_id\": 2,\n      \"questions_generated\": 30,\n      \"mappings_created\": 85,\n      \"status\": \"success\"\n    }\n  ]\n}"
                }
            ]
        },
        {
            "name": "POST /api/v1/ai/interview-questions/generate-for-skill/{skillName}",
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
                    "raw": "{{baseUrl}}/api/v1/ai/interview-questions/generate-for-skill/Java",
                    "host": ["{{baseUrl}}"],
                    "path": [
                        "api",
                        "v1",
                        "ai",
                        "interview-questions",
                        "generate-for-skill",
                        "Java"
                    ]
                },
                "description": "Generate comprehensive interview questions for a specific skill using AI"
            },
            "response": [
                {
                    "name": "Success Response",
                    "originalRequest": {
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
                            "raw": "{{baseUrl}}/api/v1/ai/interview-questions/generate-for-skill/Java",
                            "host": ["{{baseUrl}}"],
                            "path": [
                                "api",
                                "v1",
                                "ai",
                                "interview-questions",
                                "generate-for-skill",
                                "Java"
                            ]
                        }
                    },
                    "status": "OK",
                    "code": 200,
                    "_postman_previewlanguage": "json",
                    "header": [
                        {
                            "key": "Content-Type",
                            "value": "application/json"
                        }
                    ],
                    "cookie": [],
                    "body": "{\n  \"skill_name\": \"Java\",\n  \"skill_id\": 1,\n  \"questions_generated\": 30,\n  \"mappings_created\": 90,\n  \"status\": \"success\"\n}"
                }
            ]
        }
    ]

    # Add the new endpoints to the AIController
    ai_controller['item'].extend(new_endpoints)

    # Update the collection info
    collection['info']['updatedAt'] = datetime.utcnow().isoformat() + 'Z'
    collection['info']['description'] = collection.get('info', {}).get('description', '') + '\n\nUpdated with comprehensive interview question endpoints'

    # Write the updated collection back to file
    try:
        with open('Tymbl.postman_collection.json', 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        print("✅ Successfully updated Postman collection with comprehensive interview question endpoints")
        print(f"📝 Added {len(new_endpoints)} new endpoints to AIController")
        return True
    except Exception as e:
        print(f"Error writing updated collection: {e}")
        return False

if __name__ == "__main__":
    print("🔄 Updating Postman collection with comprehensive interview question endpoints...")
    success = update_postman_collection()
    if success:
        print("🎉 Postman collection update completed successfully!")
    else:
        print("❌ Postman collection update failed!")
        sys.exit(1) 