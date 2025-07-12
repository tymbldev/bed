#!/bin/bash

# Interview Preparation System Test Script
# This script demonstrates the new flat schema interview preparation system

BASE_URL="http://localhost:8085/tymbl-service"
API_BASE="$BASE_URL/api/v1/interview-prep"

echo "=== Interview Preparation System Test ==="
echo "Base URL: $BASE_URL"
echo "API Base: $API_BASE"
echo ""

# Note: These endpoints require authentication
# You'll need to add proper JWT tokens for actual testing

echo "1. Testing Designations Endpoint"
echo "GET $API_BASE/designations"
echo "Expected: List of all available designations"
echo ""

echo "2. Testing Topics by Designation"
echo "GET $API_BASE/designations/Software%20Engineer/topics"
echo "Expected: List of topics for Software Engineer"
echo ""

echo "3. Testing Topics with Filters"
echo "GET $API_BASE/designations/Software%20Engineer/topics/filter?difficultyLevel=INTERMEDIATE"
echo "Expected: Intermediate difficulty topics for Software Engineer"
echo ""

echo "4. Testing Categories by Designation"
echo "GET $API_BASE/designations/Data%20Scientist/categories"
echo "Expected: Categories available for Data Scientist"
echo ""

echo "5. Testing General Questions"
echo "GET $API_BASE/designations/Software%20Engineer/questions/general"
echo "Expected: General questions for Software Engineer"
echo ""

echo "6. Testing General Questions by Topic"
echo "GET $API_BASE/designations/Software%20Engineer/topics/Data%20Structures%20%26%20Algorithms/questions/general"
echo "Expected: General questions for Data Structures & Algorithms topic"
echo ""

echo "7. Testing Company Questions"
echo "GET $API_BASE/companies/Google/designations/Software%20Engineer/questions"
echo "Expected: Google-specific questions for Software Engineer"
echo ""

echo "8. Testing Question Generation (General)"
echo "POST $API_BASE/questions/generate"
echo "Content-Type: application/json"
echo '{
  "requestType": "GENERAL",
  "designation": "Software Engineer",
  "topicName": "Data Structures & Algorithms",
  "difficultyLevel": "INTERMEDIATE",
  "numQuestions": 3
}'
echo "Expected: Queue ID for generation request"
echo ""

echo "9. Testing Question Generation (Company-Specific)"
echo "POST $API_BASE/questions/generate"
echo "Content-Type: application/json"
echo '{
  "requestType": "COMPANY_SPECIFIC",
  "designation": "Software Engineer",
  "companyName": "Google",
  "topicName": "System Design",
  "difficultyLevel": "ADVANCED",
  "numQuestions": 2
}'
echo "Expected: Queue ID for company-specific generation request"
echo ""

echo "10. Testing Generation Queue Status"
echo "GET $API_BASE/questions/generation/queue/1"
echo "Expected: Status of generation request"
echo ""

echo "11. Testing Statistics"
echo "GET $API_BASE/designations/Software%20Engineer/statistics"
echo "Expected: Statistics for Software Engineer designation"
echo ""

echo "=== Sample cURL Commands (with authentication) ==="
echo ""
echo "# Get all designations"
echo "curl -X GET \"$API_BASE/designations\" \\"
echo "  -H \"Authorization: Bearer YOUR_JWT_TOKEN\""
echo ""
echo "# Get topics for Software Engineer"
echo "curl -X GET \"$API_BASE/designations/Software%20Engineer/topics\" \\"
echo "  -H \"Authorization: Bearer YOUR_JWT_TOKEN\""
echo ""
echo "# Generate questions"
echo "curl -X POST \"$API_BASE/questions/generate\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -H \"Authorization: Bearer YOUR_JWT_TOKEN\" \\"
echo "  -d '{\"requestType\":\"GENERAL\",\"designation\":\"Software Engineer\",\"topicName\":\"Data Structures & Algorithms\",\"difficultyLevel\":\"INTERMEDIATE\",\"numQuestions\":3}'"
echo ""

echo "=== Database Schema Summary ==="
echo ""
echo "Tables Created:"
echo "1. interview_topics - Topics organized by designation"
echo "2. general_interview_questions - General questions by designation/topic"
echo "3. company_interview_questions - Company-specific questions"
echo "4. question_generation_queue - Queue for GenAI generation requests"
echo ""
echo "Sample Data Loaded:"
echo "- 8 designations with 5-8 topics each"
echo "- Difficulty levels: BEGINNER, INTERMEDIATE, ADVANCED"
echo "- Categories: TECHNICAL, BEHAVIORAL, PRODUCT"
echo "- Estimated preparation time for each topic"
echo ""

echo "=== Key Features ==="
echo "✅ Flat schema design (no foreign keys)"
echo "✅ Designation-wise topic organization"
echo "✅ General and company-specific questions"
echo "✅ GenAI-powered question generation"
echo "✅ Async processing with queue management"
echo "✅ HTML-formatted rich content"
echo "✅ Difficulty level filtering"
echo "✅ Comprehensive API documentation"
echo ""

echo "=== Next Steps ==="
echo "1. Run database migration: src/main/resources/db/interview_prep_revamp.sql"
echo "2. Add authentication tokens for testing"
echo "3. Test GenAI question generation"
echo "4. Integrate with frontend application"
echo "5. Monitor generation success rates"
echo ""

echo "Test script completed!" 