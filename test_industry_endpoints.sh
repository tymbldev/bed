#!/bin/bash

# Test Industry Endpoints Script
# This script tests the Industry dropdown endpoints

echo "🧪 Testing Industry Endpoints..."
echo "=================================="

# Base URL
BASE_URL="http://localhost:8080/api/v1/dropdowns"

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

# Test 1: Get all industries
echo -e "\n${YELLOW}Test 1: Get all industries${NC}"
echo "GET $BASE_URL/industries"
response=$(make_request "GET" "/industries")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Retrieved industries list${NC}"
    echo "Response preview:"
    echo "$response" | head -c 200
    echo "..."
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
fi

# Test 2: Get industries as map
echo -e "\n${YELLOW}Test 2: Get industries as map${NC}"
echo "GET $BASE_URL/industries-map"
response=$(make_request "GET" "/industries-map")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Retrieved industries map${NC}"
    echo "Response preview:"
    echo "$response" | head -c 200
    echo "..."
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
fi

# Test 3: Create a new industry
echo -e "\n${YELLOW}Test 3: Create a new industry${NC}"
echo "POST $BASE_URL/industries"
new_industry='{
  "name": "Test Industry",
  "description": "A test industry for testing purposes"
}'
response=$(make_request "POST" "/industries" "$new_industry")
status_code=$?

if [ $status_code -eq 200 ]; then
    echo -e "${GREEN}✅ Success: Created new industry${NC}"
    echo "Response:"
    echo "$response"
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
    echo "Response:"
    echo "$response"
fi

# Test 4: Verify the new industry appears in the list
echo -e "\n${YELLOW}Test 4: Verify new industry in list${NC}"
echo "GET $BASE_URL/industries"
response=$(make_request "GET" "/industries")
status_code=$?

if [ $status_code -eq 200 ]; then
    if echo "$response" | grep -q "Test Industry"; then
        echo -e "${GREEN}✅ Success: New industry found in list${NC}"
    else
        echo -e "${YELLOW}⚠️  Warning: New industry not found in list${NC}"
    fi
else
    echo -e "${RED}❌ Failed: Status code $status_code${NC}"
fi

echo -e "\n${GREEN}🎉 Industry endpoints testing completed!${NC}"
echo "==================================" 