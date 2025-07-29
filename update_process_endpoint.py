#!/usr/bin/env python3
import json

def update_process_endpoint():
    # Read current collection
    with open('Tymbl.postman_collection.json', 'r') as f:
        collection = json.load(f)
    
    # Find AI Company Controller section
    ai_company_section = None
    for section in collection['item']:
        if section['name'] == 'AI Company Controller':
            ai_company_section = section
            break
    
    if not ai_company_section:
        print("❌ AI Company Controller section not found!")
        return
    
    # Update the process endpoints with new functionality
    updated_endpoints = [
        {
            "name": "Generate Companies Batch",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {"mode": "raw", "raw": "{\n  \"industries\": [\"Technology\", \"Finance\", \"Healthcare\"]\n}"},
                "url": {"raw": "{{base_url}}/api/v1/ai/companies/generate-batch", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "generate-batch"]}
            }
        },
        {
            "name": "Process All Operations (All Companies)",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {"mode": "raw", "raw": "{\n  \"note\": \"Processes all operations for all companies (no operations parameter specified)\"\n}"},
                "url": {"raw": "{{base_url}}/api/v1/ai/companies/process", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "process"]}
            }
        },
        {
            "name": "Process Specific Operations (All Companies)",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {"mode": "raw", "raw": "{\n  \"note\": \"Processes only crawl and detectIndustries for all companies\"\n}"},
                "url": {"raw": "{{base_url}}/api/v1/ai/companies/process?operations=crawl,detectIndustries", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "process"], "query": [{"key": "operations", "value": "crawl,detectIndustries"}]}
            }
        },
        {
            "name": "Process All Operations (Specific Company)",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {"mode": "raw", "raw": "{\n  \"note\": \"Processes all operations for specific company (no operations parameter specified)\"\n}"},
                "url": {"raw": "{{base_url}}/api/v1/ai/companies/process?companyName=Google", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "process"], "query": [{"key": "companyName", "value": "Google"}]}
            }
        },
        {
            "name": "Process Specific Operations (Specific Company)",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {"mode": "raw", "raw": "{\n  \"note\": \"Processes only crawl and cleanup for specific company\"\n}"},
                "url": {"raw": "{{base_url}}/api/v1/ai/companies/process?operations=crawl,cleanup&companyName=Google", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "process"], "query": [{"key": "operations", "value": "crawl,cleanup"}, {"key": "companyName", "value": "Google"}]}
            }
        },
        {
            "name": "Reset Company Flags (All Companies)",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {"mode": "raw", "raw": "{\n  \"note\": \"Resets flags for all companies\"\n}"},
                "url": {"raw": "{{base_url}}/api/v1/ai/companies/reset-flags?flags=is_crawled,industry_processed,content_shortened,similar_companies_processed,cleanup_processed", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "reset-flags"], "query": [{"key": "flags", "value": "is_crawled,industry_processed,content_shortened,similar_companies_processed,cleanup_processed"}]}
            }
        },
        {
            "name": "Reset Company Flags (Specific Company)",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {"mode": "raw", "raw": "{\n  \"note\": \"Resets flags for specific company\"\n}"},
                "url": {"raw": "{{base_url}}/api/v1/ai/companies/reset-flags?flags=is_crawled,industry_processed&companyName=Google", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "reset-flags"], "query": [{"key": "flags", "value": "is_crawled,industry_processed"}, {"key": "companyName", "value": "Google"}]}
            }
        },
        {
            "name": "Generate Shortnames for All Companies",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {"mode": "raw", "raw": "{\n  \"note\": \"Generates shortnames for all companies using AI\"\n}"},
                "url": {"raw": "{{base_url}}/api/v1/ai/companies/shortnames/generate-for-all", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "shortnames", "generate-for-all"]}
            }
        },
        {
            "name": "Generate Shortname for Specific Company",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {"mode": "raw", "raw": "{\n  \"note\": \"Generates shortname for specific company using AI\"\n}"},
                "url": {"raw": "{{base_url}}/api/v1/ai/companies/shortnames/generate-for-company/Google", "host": ["{{base_url}}"], "path": ["api", "v1", "ai", "companies", "shortnames", "generate-for-company", "Google"]}
            }
        }
    ]
    
    # Replace the existing endpoints
    ai_company_section['item'] = updated_endpoints
    
    # Save updated collection
    with open('Tymbl.postman_collection.json', 'w') as f:
        json.dump(collection, f, indent=2)
    
    print("✅ Updated AI Company Controller process endpoint with new functionality!")
    print("📊 Updated endpoints:")
    for endpoint in updated_endpoints:
        print(f"   - {endpoint['name']}")
    
    print("\n🎯 New Process Endpoint Features:")
    print("   - Can specify specific operations: crawl, detectIndustries, shortenContent, similarCompanies, cleanup")
    print("   - Can process all companies or specific company")
    print("   - If no operations specified, processes all operations")
    print("   - Validates operations and returns error for invalid ones")
    
    # Count total endpoints
    total_endpoints = sum(len(section['item']) for section in collection['item'])
    print(f"🎯 Total endpoints in collection: {total_endpoints}")

if __name__ == "__main__":
    update_process_endpoint() 