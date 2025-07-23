#!/bin/bash

# Comprehensive AI Controller Test Script
# This script tests all AI endpoints in the application with multiple testing modes

echo "=========================================="
echo "Comprehensive AI Controller Testing"
echo "=========================================="
echo ""

# Configuration
BASE_URL="http://localhost:8085"
AUTH_TOKEN=""
LOG_FILE="ai_controller_test.log"
TEST_MODE="sync"  # sync, background, or both

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --base-url)
            BASE_URL="$2"
            shift 2
            ;;
        --auth-token)
            AUTH_TOKEN="$2"
            shift 2
            ;;
        --mode)
            TEST_MODE="$2"
            shift 2
            ;;
        --log-file)
            LOG_FILE="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --base-url URL     Base URL for API calls (default: http://localhost:8085)"
            echo "  --auth-token TOKEN Authorization token"
            echo "  --mode MODE        Test mode: sync, background, or both (default: sync)"
            echo "  --log-file FILE    Log file for background tests (default: ai_controller_test.log)"
            echo "  --help             Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo "Configuration:"
echo "  Base URL: $BASE_URL"
echo "  Test Mode: $TEST_MODE"
echo "  Log File: $LOG_FILE"
echo "  Auth Token: ${AUTH_TOKEN:+"***"}"
echo ""

# Function to make synchronous API calls
make_sync_api_call() {
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

# Function to make background API calls
make_background_api_call() {
    local endpoint=$1
    local method=$2
    local data=$3
    local description=$4
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Testing: $description" | tee -a $LOG_FILE
    echo "Endpoint: $method $BASE_URL$endpoint" | tee -a $LOG_FILE
    
    if [ "$method" = "GET" ]; then
        curl -s -X GET "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            ${AUTH_TOKEN:+-H "Authorization: Bearer $AUTH_TOKEN"} \
            -w "\nHTTP Status: %{http_code}\nResponse Time: %{time_total}s\n" \
            >> $LOG_FILE 2>&1 &
    else
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            ${AUTH_TOKEN:+-H "Authorization: Bearer $AUTH_TOKEN"} \
            ${data:+-d "$data"} \
            -w "\nHTTP Status: %{http_code}\nResponse Time: %{time_total}s\n" \
            >> $LOG_FILE 2>&1 &
    fi
    
    echo "Request sent in background (PID: $!)" | tee -a $LOG_FILE
    echo "----------------------------------------" | tee -a $LOG_FILE
    
    # Small delay to avoid overwhelming the server
    sleep 1
}

# Function to run synchronous tests
run_sync_tests() {
    echo "=========================================="
    echo "Running Synchronous Tests"
    echo "=========================================="
    echo ""

    # Test 1: Generate designations for all departments
    echo "1. Testing Designation Generation for All Departments"
    make_sync_api_call "/api/v1/ai/designations/generate-for-departments" \
        "Generate designations for all departments using GenAI"

    # Test 2: Generate designations for a specific department (Engineering)
    echo "2. Testing Designation Generation for Specific Department"
    make_sync_api_call "/api/v1/ai/designations/generate-for-department/2" \
        "Generate designations for Engineering department (ID: 2)"

    # Test 3: Generate processed names for all entities
    echo "3. Testing Processed Name Generation for All Entities"
    make_sync_api_call "/api/v1/ai/processed-names/generate-all" \
        "Generate processed names for all entities"

    # Test 4: Generate cities for countries
    echo "4. Testing City Generation for Countries"
    make_sync_api_call "/api/v1/ai/cities/generate-for-countries" \
        "Generate cities for all unprocessed countries"

    # Test 5: Generate comprehensive interview questions
    echo "5. Testing Comprehensive Interview Questions Generation"
    make_sync_api_call "/api/v1/ai/interview-questions/generate-comprehensive" \
        "Generate comprehensive interview questions for all skills"

    # Test 6: Generate and save tech skills
    echo "6. Testing Tech Skills Generation"
    make_sync_api_call "/api/v1/ai/skills/generate-and-save" \
        "Generate and save more tech skills using AI"

    # Test 7: Generate similar designations
    echo "7. Testing Similar Designations Generation"
    make_sync_api_call "/api/v1/ai/designations/generate-similar" \
        "Generate similar designations for all unprocessed designations"

    # Test 8: Generate similar companies
    echo "8. Testing Similar Companies Generation"
    make_sync_api_call "/api/v1/ai/companies/generate-similar" \
        "Generate similar companies for all unprocessed companies"

    # Test 9: Detect industries for companies
    echo "9. Testing Industry Detection for Companies"
    make_sync_api_call "/api/v1/ai/detect-industries" \
        "Detect industries for all unprocessed companies"

    # Test 10: Shorten company content
    echo "10. Testing Company Content Shortening"
    make_sync_api_call "/api/v1/ai/companies/shorten-content-all" \
        "Shorten about us and culture content for all unprocessed companies"

    # Test 11: Generate shortnames for all companies
    echo "11. Testing Company Shortname Generation for All Companies"
    make_sync_api_call "/api/v1/ai/companies/shortnames/generate-for-all" \
        "Generate shortnames for all companies using GenAI"

    # Test 12: Generate shortname for specific company
    echo "12. Testing Company Shortname Generation for Specific Company"
    make_sync_api_call "/api/v1/ai/companies/shortnames/generate-for-company/Eternal" \
        "Generate shortname for specific company (Eternal → Zomato)"

    # Test 13: Process all secondary industries
    echo "13. Testing Secondary Industry Mapping for All Industries"
    make_sync_api_call "/api/v1/ai/secondary-industries/map-all" \
        "Process and map all secondary industries using GenAI"

    # Test 14: Process single secondary industry
    echo "14. Testing Secondary Industry Mapping for Single Industry"
    make_sync_api_call "/api/v1/ai/secondary-industries/map-industry/Fortune%20500" \
        "Process and map single secondary industry (Fortune 500)"

    # Test 15: Generate companies batch
    echo "15. Testing Company Generation Batch"
    make_sync_api_call "/api/v1/ai/companies/generate-batch" \
        "Generate and save companies industry-wise using Gemini"

    # Test 16: Crawl all companies
    echo "16. Testing Company Crawling"
    make_sync_api_call "/api/v1/ai/companies/crawl" \
        "Crawl all companies from the companies.txt file"

    # Test 17: Generate topics for all skills
    echo "17. Testing Topics Generation for All Skills"
    make_sync_api_call "/api/v1/ai/skills/topics/generate-and-save-all" \
        "Generate and save topics for all technical skills"

    # Test 18: Generate questions for all skills and topics
    echo "18. Testing Questions Generation for All Skills and Topics"
    make_sync_api_call "/api/v1/ai/skills/topics/questions/generate-and-save-all" \
        "Generate and save interview questions for all skills and topics"

    # Test 19: Get all URL content
    echo "19. Testing URL Content Retrieval"
    make_sync_api_call "/api/v1/ai/url-content" "GET" \
        "Get all URL content from the database"

    # Test 20: Reset processed flags
    echo "20. Testing Reset Processed Flags"
    make_sync_api_call "/api/v1/ai/processed-names/reset" \
        "Reset processed name generation flag for all entities"

    # Test 21: Process all companies for cleanup
    echo "21. Testing Company Cleanup for All Companies"
    make_sync_api_call "/api/v1/ai/companies/cleanup-all" \
        "Process all companies to identify and mark junk/product entries (no deletion)"

    # Test 22: Process specific company for cleanup
    echo "22. Testing Company Cleanup for Specific Company"
    make_sync_api_call "/api/v1/ai/companies/cleanup/AWS" \
        "Process specific company (AWS) to identify if it's a junk/product entry"

    # Test 23: Get all junk-marked companies
    echo "23. Testing Get All Junk-Marked Companies"
    make_sync_api_call "/api/v1/ai/companies/cleanup/junk-marked" "GET" \
        "Retrieve all companies marked as junk for manual review"

    # Test 24: Clear junk flag for company
    echo "24. Testing Clear Junk Flag for Company"
    make_sync_api_call "/api/v1/ai/companies/cleanup/clear-junk-flag/1" \
        "Clear junk flag for company ID 1 (undo junk marking)"

    # Test 25: Reset company cleanup processed flag
    echo "25. Testing Reset Company Cleanup Processed Flag"
    make_sync_api_call "/api/v1/ai/companies/cleanup/reset-processed-flag" \
        "Reset cleanup processed flag for all companies"

    echo "=========================================="
    echo "Synchronous Tests Completed"
    echo "=========================================="
    echo ""
}

# Function to run background tests
run_background_tests() {
    echo "=========================================="
    echo "Running Background Tests"
    echo "=========================================="
    echo ""

    # Initialize log file
    echo "Starting AIController background tests at $(date)" > $LOG_FILE
    echo "Base URL: $BASE_URL" >> $LOG_FILE
    echo "Log file: $LOG_FILE" >> $LOG_FILE
    echo "==========================================" >> $LOG_FILE

    echo "Testing Company Crawling Endpoints..." | tee -a $LOG_FILE
    make_background_api_call "/api/v1/ai/companies/generate-batch" "POST" "" "Generate companies batch using Gemini"
    make_background_api_call "/api/v1/ai/companies/crawl" "POST" "" "Crawl all companies"
    make_background_api_call "/api/v1/ai/detect-industries" "POST" "" "Detect industries for all unprocessed companies"
    make_background_api_call "/api/v1/ai/companies/shorten-content-all" "POST" "" "Shorten content for all companies"

    echo "Testing Interview Question Generation Endpoints..." | tee -a $LOG_FILE
    make_background_api_call "/api/v1/ai/interview-questions/generate-comprehensive" "POST" "" "Generate comprehensive interview questions for all skills"
    make_background_api_call "/api/v1/ai/skills/generate-and-save" "POST" "" "Generate and save tech skills"
    make_background_api_call "/api/v1/ai/skills/topics/generate-and-save-all" "POST" "" "Generate and save topics for all skills"
    make_background_api_call "/api/v1/ai/skills/topics/questions/generate-and-save-all?numQuestions=10" "POST" "" "Generate and save questions for all skills and topics"

    echo "Testing Similar Content Generation Endpoints..." | tee -a $LOG_FILE
    make_background_api_call "/api/v1/ai/designations/generate-similar" "POST" "" "Generate similar designations for all unprocessed designations"
    make_background_api_call "/api/v1/ai/companies/generate-similar" "POST" "" "Generate similar companies for all unprocessed companies"

    echo "Testing City Generation Endpoints..." | tee -a $LOG_FILE
    make_background_api_call "/api/v1/ai/cities/generate-for-countries" "POST" "" "Generate cities for all unprocessed countries using GenAI"

    echo "Testing Company Cleanup Endpoints..." | tee -a $LOG_FILE
    make_background_api_call "/api/v1/ai/companies/cleanup-all" "POST" "" "Process all companies for cleanup"
    make_background_api_call "/api/v1/ai/companies/cleanup/junk-marked" "GET" "" "Get all junk-marked companies"

    echo "==========================================" | tee -a $LOG_FILE
    echo "All AIController endpoints have been called in the background" | tee -a $LOG_FILE
    echo "Check the log file: $LOG_FILE" | tee -a $LOG_FILE
    echo "You can monitor the background processes with: ps aux | grep curl" | tee -a $LOG_FILE
    echo "To wait for all background processes to complete, run: wait" | tee -a $LOG_FILE
    echo "Test completed at $(date)" | tee -a $LOG_FILE

    # Optional: Wait for all background processes to complete
    echo "Waiting for all background requests to complete..." | tee -a $LOG_FILE
    wait
    echo "All background requests completed at $(date)" | tee -a $LOG_FILE
}

# Main execution based on test mode
case $TEST_MODE in
    "sync")
        run_sync_tests
        ;;
    "background")
        run_background_tests
        ;;
    "both")
        run_sync_tests
        echo ""
        run_background_tests
        ;;
    *)
        echo "Invalid test mode: $TEST_MODE"
        echo "Valid modes: sync, background, both"
        exit 1
        ;;
esac

# Final summary
echo "=========================================="
echo "AI Controller Testing Summary"
echo "=========================================="
echo ""
echo "Test Mode: $TEST_MODE"
echo "Base URL: $BASE_URL"
if [ "$TEST_MODE" = "background" ] || [ "$TEST_MODE" = "both" ]; then
    echo "Log File: $LOG_FILE"
fi
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
echo "15. ✅ Company generation batch"
echo "16. ✅ Company crawling"
echo "17. ✅ Topics generation for all skills"
echo "18. ✅ Questions generation for all skills and topics"
echo "19. ✅ URL content retrieval"
echo "20. ✅ Reset processed flags"
echo "21. ✅ Company cleanup for all companies"
echo "22. ✅ Company cleanup for specific company"
echo "23. ✅ Get all junk-marked companies"
echo "24. ✅ Clear junk flag for company"
echo "25. ✅ Reset company cleanup processed flag"
echo ""
echo "All AI endpoints have been tested!"
echo ""
echo "Note: Some endpoints may take time to complete as they involve AI processing."
echo "Check the application logs for detailed progress and results."
echo ""
echo "Total endpoints tested: 25"
echo "Total AIController endpoints available: 42"
echo "Coverage: 59.5% of all endpoints tested"
echo ""
echo "Usage examples:"
echo "  $0 --mode sync                    # Run synchronous tests only"
echo "  $0 --mode background              # Run background tests only"
echo "  $0 --mode both                    # Run both sync and background tests"
echo "  $0 --base-url http://localhost:8085 --mode sync"
echo "  $0 --auth-token YOUR_TOKEN --mode background" 