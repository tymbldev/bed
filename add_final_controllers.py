#!/usr/bin/env python3
import json

def add_final_controllers():
    # Read current collection
    with open('Tymbl.postman_collection.json', 'r') as f:
        collection = json.load(f)
    
    # Add final missing controllers
    final_controllers = [
        {
            "name": "Registration Controller",
            "item": [
                {
                    "name": "Register User",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"email\":\"user@example.com\",\"password\":\"password\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"},
                        "url": {"raw": "{{base_url}}/api/v1/registration", "host": ["{{base_url}}"], "path": ["api", "v1", "registration"]}
                    }
                }
            ]
        },
        {
            "name": "Interview Generation Controller",
            "item": [
                {
                    "name": "Generate Topics for Designation",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"topics\":[\"Data Structures\",\"Algorithms\"]}"},
                        "url": {"raw": "{{base_url}}/api/v1/interview-generation/designations/Software%20Engineer/topics/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "designations", "Software Engineer", "topics", "generate"]}
                    }
                },
                {
                    "name": "Generate and Save Topics for Designation",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"topics\":[\"Data Structures\",\"Algorithms\"]}"},
                        "url": {"raw": "{{base_url}}/api/v1/interview-generation/designations/Software%20Engineer/topics/generate-and-save", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "designations", "Software Engineer", "topics", "generate-and-save"]}
                    }
                },
                {
                    "name": "Generate All Topics",
                    "request": {
                        "method": "POST",
                        "url": {"raw": "{{base_url}}/api/v1/interview-generation/designations/topics/generate-all", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "designations", "topics", "generate-all"]}
                    }
                },
                {
                    "name": "Generate and Save All Topics",
                    "request": {
                        "method": "POST",
                        "url": {"raw": "{{base_url}}/api/v1/interview-generation/designations/topics/generate-and-save-all", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "designations", "topics", "generate-and-save-all"]}
                    }
                },
                {
                    "name": "Generate Questions",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"topics\":[\"Data Structures\"],\"designation\":\"Software Engineer\"}"},
                        "url": {"raw": "{{base_url}}/api/v1/interview-generation/questions/generate", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "questions", "generate"]}
                    }
                },
                {
                    "name": "Get Generation Queue by ID",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/interview-generation/questions/generation/queue/1", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "questions", "generation", "queue", "1"]}
                    }
                },
                {
                    "name": "Get Pending Generation Queue",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/interview-generation/questions/generation/queue/pending", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "questions", "generation", "queue", "pending"]}
                    }
                },
                {
                    "name": "Get In Progress Generation Queue",
                    "request": {
                        "method": "GET",
                        "url": {"raw": "{{base_url}}/api/v1/interview-generation/questions/generation/queue/in-progress", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "questions", "generation", "queue", "in-progress"]}
                    }
                },
                {
                    "name": "Generate and Save Questions for Skill",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"topics\":[\"Java Programming\"]}"},
                        "url": {"raw": "{{base_url}}/api/v1/interview-generation/skills/Java/topics/questions/generate-and-save", "host": ["{{base_url}}"], "path": ["api", "v1", "interview-generation", "skills", "Java", "topics", "questions", "generate-and-save"]}
                    }
                }
            ]
        },
        {
            "name": "My Job Applications Controller",
            "item": [
                {
                    "name": "Apply for Job",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"jobId\":1,\"jobReferrerId\":1}"},
                        "url": {"raw": "{{base_url}}/api/v1/my-applications", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications"]}
                    }
                },
                {
                    "name": "Get My Applications",
                    "request": {
                        "method": "GET",
                        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                        "url": {"raw": "{{base_url}}/api/v1/my-applications", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications"]}
                    }
                },
                {
                    "name": "Submit Referrer Feedback",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}, {"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{\"jobId\":1,\"referrerUserId\":1,\"feedbackText\":\"Great experience\",\"score\":5}"},
                        "url": {"raw": "{{base_url}}/api/v1/my-applications/feedback", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications", "feedback"]}
                    }
                },
                {
                    "name": "Switch Referrer",
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
                        "url": {"raw": "{{base_url}}/api/v1/my-applications/switch-referrer?applicationId=1&newJobReferrerId=2", "host": ["{{base_url}}"], "path": ["api", "v1", "my-applications", "switch-referrer"], "query": [{"key": "applicationId", "value": "1"}, {"key": "newJobReferrerId", "value": "2"}]}
                    }
                }
            ]
        }
    ]
    
    # Add final controllers to collection
    collection['item'].extend(final_controllers)
    
    # Save updated collection
    with open('Tymbl.postman_collection.json', 'w') as f:
        json.dump(collection, f, indent=2)
    
    print("✅ Added final missing controllers to Postman collection!")
    print("📊 Added controllers:")
    for controller in final_controllers:
        print(f"   - {controller['name']} ({len(controller['item'])} endpoints)")
    
    # Count total endpoints
    total_endpoints = sum(len(section['item']) for section in collection['item'])
    print(f"🎯 Total endpoints now: {total_endpoints}")
    
    # Verify we have all controllers
    total_controllers = len(collection['item'])
    print(f"📋 Total controllers: {total_controllers}")

if __name__ == "__main__":
    add_final_controllers() 