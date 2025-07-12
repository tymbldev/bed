#!/bin/bash

# Test script for the new designation generation endpoint
echo "Testing designation generation endpoint..."

# Base URL - using correct port and context path
BASE_URL="http://localhost:8085/tymbl-service/api/v1/dropdowns"

echo "1. Testing single department designation generation (Engineering department)..."
curl -X GET "$BASE_URL/designations/generate/2" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\n2. Testing Marketing department designation generation..."
curl -X GET "$BASE_URL/designations/generate/5" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\n3. Testing Product department designation generation..."
curl -X GET "$BASE_URL/designations/generate/3" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\n4. Testing Data Science department designation generation..."
curl -X GET "$BASE_URL/designations/generate/14" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\nTest completed!" 