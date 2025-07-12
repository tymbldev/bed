#!/bin/bash

# Test Elasticsearch Job Search Script
# This script tests the new unified job search endpoint

echo "🧪 Testing Elasticsearch Job Search..."
echo "======================================"

# Base URL
BASE_URL="http://localhost:8080/api/v1/jobsearch"

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

# Test 1: Basic search with keywords
echo -e "\n${YELLOW}Test 1: Basic Keyword Search${NC}"
echo "POST $BASE_URL/search"
search_request='{
  "keywords": ["software", "engineer"],
  "page": 0,
  "size": 10
}'
response=$(make_request "POST" "/search" "$search_request")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Basic search completed${NC}"
    echo "Response preview:"
    echo "$response" | head -c 500
    echo "..."
    
    # Count results
    total=$(echo "$response" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}📊 Total results: $total${NC}"
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
    echo "Response:"
    echo "$response"
fi

# Test 2: Search with location and experience filters
echo -e "\n${YELLOW}Test 2: Search with Filters${NC}"
echo "POST $BASE_URL/search"
search_request='{
  "keywords": ["java"],
  "cityId": 1,
  "countryId": 1,
  "minExperience": 2,
  "maxExperience": 5,
  "page": 0,
  "size": 5
}'
response=$(make_request "POST" "/search" "$search_request")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Filtered search completed${NC}"
    echo "Response preview:"
    echo "$response" | head -c 500
    echo "..."
    
    # Count results
    total=$(echo "$response" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}📊 Total results: $total${NC}"
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
    echo "Response:"
    echo "$response"
fi

# Test 3: Search with multiple keywords
echo -e "\n${YELLOW}Test 3: Multiple Keywords Search${NC}"
echo "POST $BASE_URL/search"
search_request='{
  "keywords": ["python", "data", "analytics"],
  "page": 0,
  "size": 10
}'
response=$(make_request "POST" "/search" "$search_request")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Multiple keywords search completed${NC}"
    echo "Response preview:"
    echo "$response" | head -c 500
    echo "..."
    
    # Count results
    total=$(echo "$response" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}📊 Total results: $total${NC}"
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
    echo "Response:"
    echo "$response"
fi

# Test 4: Search with only experience filter
echo -e "\n${YELLOW}Test 4: Experience Filter Only${NC}"
echo "POST $BASE_URL/search"
search_request='{
  "minExperience": 3,
  "maxExperience": 7,
  "page": 0,
  "size": 10
}'
response=$(make_request "POST" "/search" "$search_request")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Experience filter search completed${NC}"
    echo "Response preview:"
    echo "$response" | head -c 500
    echo "..."
    
    # Count results
    total=$(echo "$response" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}📊 Total results: $total${NC}"
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
    echo "Response:"
    echo "$response"
fi

# Test 5: Reindex all jobs
echo -e "\n${YELLOW}Test 5: Reindex All Jobs${NC}"
echo "POST $BASE_URL/reindex"
response=$(make_request "POST" "/reindex")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Reindex completed${NC}"
    echo "Response:"
    echo "$response"
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
    echo "Response:"
    echo "$response"
fi

# Test 6: Get all jobs (traditional endpoint)
echo -e "\n${YELLOW}Test 6: Get All Jobs (Traditional)${NC}"
echo "GET $BASE_URL"
response=$(make_request "GET" "")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Retrieved all jobs${NC}"
    echo "Response preview:"
    echo "$response" | head -c 300
    echo "..."
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
fi

echo -e "\n${GREEN}🎉 Elasticsearch job search testing completed!${NC}"
echo "======================================" 