#!/bin/bash

# Test Industry Detection Endpoint Script
# This script tests the company industry detection endpoint

echo "🧪 Testing Industry Detection Endpoint..."
echo "=========================================="

# Base URL
BASE_URL="http://localhost:8080/api/v1/companies"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to make HTTP requests
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    fi
    
    # Split response into body and status code
    body=$(echo "$response" | head -n -1)
    status_code=$(echo "$response" | tail -n 1)
    
    echo "$body"
    return $status_code
}

# Test 1: Detect industries using manual detection (no Gemini)
echo -e "\n${YELLOW}Test 1: Manual Industry Detection${NC}"
echo "POST $BASE_URL/detect-industries?useGemini=false"
response=$(make_request "POST" "/detect-industries?useGemini=false")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Manual industry detection completed${NC}"
    echo "Response preview:"
    echo "$response" | head -c 500
    echo "..."
    
    # Count processed companies
    processed_count=$(echo "$response" | grep -o '"processed":true' | wc -l)
    echo -e "${GREEN}📊 Processed companies: $processed_count${NC}"
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
    echo "Response:"
    echo "$response"
fi

# Test 2: Detect industries using Gemini AI (if available)
echo -e "\n${YELLOW}Test 2: Gemini AI Industry Detection${NC}"
echo "POST $BASE_URL/detect-industries?useGemini=true"
response=$(make_request "POST" "/detect-industries?useGemini=true")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Gemini AI industry detection completed${NC}"
    echo "Response preview:"
    echo "$response" | head -c 500
    echo "..."
    
    # Count processed companies
    processed_count=$(echo "$response" | grep -o '"processed":true' | wc -l)
    echo -e "${GREEN}📊 Processed companies: $processed_count${NC}"
    
    # Show some example results
    echo -e "\n${YELLOW}Example Results:${NC}"
    echo "$response" | grep -A 5 -B 5 '"processed":true' | head -20
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
    echo "Response:"
    echo "$response"
fi

# Test 3: Get companies to verify industry data was saved
echo -e "\n${YELLOW}Test 3: Verify Companies with Industry Data${NC}"
echo "GET $BASE_URL"
response=$(make_request "GET" "")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Retrieved companies list${NC}"
    echo "Response preview:"
    echo "$response" | head -c 300
    echo "..."
    
    # Check if companies have industry data
    companies_with_industry=$(echo "$response" | grep -o '"primaryIndustryId"' | wc -l)
    echo -e "${GREEN}📊 Companies with industry data: $companies_with_industry${NC}"
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
fi

echo -e "\n${GREEN}🎉 Industry detection testing completed!${NC}"
echo "==========================================" 