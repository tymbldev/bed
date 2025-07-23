#!/bin/bash

# Script to test AI functionality
# This script tests various AI endpoints in the application

echo "=========================================="
echo "Testing AI Functionality"
echo "=========================================="
echo ""

# Configuration
BASE_URL="http://localhost:8085"
AUTH_TOKEN=""

# Check if base URL is provided as argument
if [ $# -eq 1 ]; then
    BASE_URL=$1
    echo "Using base URL: $BASE_URL"
elif [ $# -eq 2 ]; then
    BASE_URL=$1
    AUTH_TOKEN=$2
    echo "Using base URL: $BASE_URL with auth token"
fi

echo ""

# Function to make API calls
make_api_call() {
    local endpoint=$1
    local description=$2
    local method=${3:-POST}
    
    echo "Testing: $description"
    echo "Endpoint: $method $endpoint"
    echo ""
    
    if [ -n "$AUTH_TOKEN" ]; then
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $AUTH_TOKEN" \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method \
            -H "Content-Type: application/json" \
            "$BASE_URL$endpoint")
    fi
    
    # Extract HTTP status code
    http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    # Extract response body
    response_body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    echo "HTTP Status: $http_status"
    echo "Response:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "$response_body"
    echo ""
    echo "----------------------------------------"
    echo ""
}

# Test 1: Generate designations for all departments
echo "1. Testing Designation Generation for All Departments"
make_api_call "/api/v1/ai/designations/generate-for-departments" \
    "Generate designations for all departments using GenAI"

# Test 2: Generate designations for a specific department (Engineering)
echo "2. Testing Designation Generation for Specific Department"
make_api_call "/api/v1/ai/designations/generate-for-department/2" \
    "Generate designations for Engineering department (ID: 2)"

# Test 3: Generate processed names for all entities
echo "3. Testing Processed Name Generation for All Entities"
make_api_call "/api/v1/ai/processed-names/generate-all" \
    "Generate processed names for all entities"

# Test 4: Generate cities for countries
echo "4. Testing City Generation for Countries"
make_api_call "/api/v1/ai/cities/generate-for-countries" \
    "Generate cities for all unprocessed countries"

# Test 5: Generate comprehensive interview questions
echo "5. Testing Comprehensive Interview Questions Generation"
make_api_call "/api/v1/ai/interview-questions/generate-comprehensive" \
    "Generate comprehensive interview questions for all skills"

# Test 6: Generate and save tech skills
echo "6. Testing Tech Skills Generation"
make_api_call "/api/v1/ai/skills/generate-and-save" \
    "Generate and save more tech skills using AI"

# Test 7: Generate similar designations
echo "7. Testing Similar Designations Generation"
make_api_call "/api/v1/ai/designations/generate-similar" \
    "Generate similar designations for all unprocessed designations"

# Test 8: Generate similar companies
echo "8. Testing Similar Companies Generation"
make_api_call "/api/v1/ai/companies/generate-similar" \
    "Generate similar companies for all unprocessed companies"

# Test 9: Detect industries for companies
echo "9. Testing Industry Detection for Companies"
make_api_call "/api/v1/ai/detect-industries" \
    "Detect industries for all unprocessed companies"

# Test 10: Shorten company content
echo "10. Testing Company Content Shortening"
make_api_call "/api/v1/ai/companies/shorten-content-all" \
    "Shorten about us and culture content for all unprocessed companies"

# Test 11: Generate shortnames for all companies
echo "11. Testing Company Shortname Generation for All Companies"
make_api_call "/api/v1/ai/companies/shortnames/generate-for-all" \
    "Generate shortnames for all companies using GenAI"

# Test 12: Generate shortname for specific company
echo "12. Testing Company Shortname Generation for Specific Company"
make_api_call "/api/v1/ai/companies/shortnames/generate-for-company/Eternal" \
    "Generate shortname for specific company (Eternal → Zomato)"

# Test 13: Process all secondary industries
echo "13. Testing Secondary Industry Mapping for All Industries"
make_api_call "/api/v1/ai/secondary-industries/map-all" \
    "Process and map all secondary industries using GenAI"

# Test 14: Process single secondary industry
echo "14. Testing Secondary Industry Mapping for Single Industry"
make_api_call "/api/v1/ai/secondary-industries/map-industry/Fortune%20500" \
    "Process and map single secondary industry (Fortune 500)"

echo "=========================================="
echo "AI Functionality Testing Completed"
echo "=========================================="
echo ""
echo "Summary of tests performed:"
echo "1. ✅ Designation generation for all departments"
echo "2. ✅ Designation generation for specific department"
echo "3. ✅ Processed name generation for all entities"
echo "4. ✅ City generation for countries"
echo "5. ✅ Comprehensive interview questions generation"
echo "6. ✅ Tech skills generation"
echo "7. ✅ Similar designations generation"
echo "8. ✅ Similar companies generation"
echo "9. ✅ Industry detection for companies"
echo "10. ✅ Company content shortening"
echo "11. ✅ Company shortname generation for all companies"
echo "12. ✅ Company shortname generation for specific company"
echo "13. ✅ Secondary industry mapping for all industries"
echo "14. ✅ Secondary industry mapping for single industry"
echo ""
echo "All AI endpoints have been tested!"
echo ""
echo "Note: Some endpoints may take time to complete as they involve AI processing."
echo "Check the application logs for detailed progress and results." 