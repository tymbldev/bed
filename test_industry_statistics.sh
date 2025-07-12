#!/bin/bash

# Test script for Industry Statistics endpoint
# This script tests the new endpoint that returns industry statistics with company counts and top companies

BASE_URL="http://localhost:8080/api/v1"

echo "🧪 Testing Industry Statistics Endpoint"
echo "======================================"

# Test the industry statistics endpoint
echo "📊 Testing GET /dropdowns/industries/statistics"
echo "----------------------------------------------"

response=$(curl -s -w "\n%{http_code}" "${BASE_URL}/dropdowns/industries/statistics")

# Extract status code and response body
http_code=$(echo "$response" | tail -n1)
response_body=$(echo "$response" | head -n -1)

echo "Status Code: $http_code"
echo "Response:"
echo "$response_body" | jq '.' 2>/dev/null || echo "$response_body"

echo ""
echo "✅ Test completed!"
echo ""
echo "📋 Summary:"
echo "- Endpoint: GET /api/v1/dropdowns/industries/statistics"
echo "- Purpose: Returns all industries with company counts and top 5 companies in each industry"
echo "- Expected: List of industries with statistics and top companies based on active job count"
echo ""
echo "📝 Response Format:"
echo "- industryId: Industry ID"
echo "- industryName: Industry name"
echo "- industryDescription: Industry description"
echo "- companyCount: Number of companies in this industry"
echo "- topCompanies: Array of top 5 companies with:"
echo "  - companyId: Company ID"
echo "  - companyName: Company name"
echo "  - logoUrl: Company logo URL"
echo "  - website: Company website"
echo "  - headquarters: Company headquarters"
echo "  - activeJobCount: Number of active jobs for this company" 