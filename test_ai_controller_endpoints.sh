#!/bin/bash

# AIController Endpoints Test Script
# Base URL: localhost:9080/tymbl-service/
# All endpoints will be called in the background

BASE_URL="http://localhost:9080/tymbl-service/api/v1/ai"
LOG_FILE="ai_controller_test.log"

echo "Starting AIController endpoints test at $(date)" | tee -a $LOG_FILE
echo "Base URL: $BASE_URL" | tee -a $LOG_FILE
echo "Log file: $LOG_FILE" | tee -a $LOG_FILE
echo "==========================================" | tee -a $LOG_FILE

# Function to make curl request in background and log results
make_request() {
    local endpoint=$1
    local method=$2
    local data=$3
    local description=$4
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Testing: $description" | tee -a $LOG_FILE
    echo "Endpoint: $method $BASE_URL$endpoint" | tee -a $LOG_FILE
    
    if [ "$method" = "GET" ]; then
        curl -s -X GET "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -w "\nHTTP Status: %{http_code}\nResponse Time: %{time_total}s\n" \
            >> $LOG_FILE 2>&1 &
    else
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            ${data:+-d "$data"} \
            -w "\nHTTP Status: %{http_code}\nResponse Time: %{time_total}s\n" \
            >> $LOG_FILE 2>&1 &
    fi
    
    echo "Request sent in background (PID: $!)" | tee -a $LOG_FILE
    echo "----------------------------------------" | tee -a $LOG_FILE
    
    # Small delay to avoid overwhelming the server
    sleep 1
}

# ============================================================================
# COMPANY CRAWLING ENDPOINTS
# ============================================================================

echo "Testing Company Crawling Endpoints..." | tee -a $LOG_FILE

# Generate companies batch
make_request "/companies/generate-batch" "POST" "" "Generate companies batch using Gemini"

# Crawl all companies
make_request "/companies/crawl" "POST" "" "Crawl all companies"

# Crawl jobs for specific company (using company ID 1)
make_request "/companies/1/jobs/crawl" "POST" "" "Crawl jobs for company ID 1"

# Crawl jobs for company by name
make_request "/companies/Google/jobs/crawl-by-name" "POST" "" "Crawl jobs for company by name (Google)"

# Reset industry processed flag for all companies
make_request "/detect-industries/reset" "POST" "" "Reset industry processed flag for all companies"

# Detect industries for all companies
make_request "/detect-industries" "POST" "" "Detect industries for all unprocessed companies"

# Shorten content for specific company
make_request "/companies/1/shorten-content" "POST" "" "Shorten content for company ID 1"

# Shorten content for all companies
make_request "/companies/shorten-content-all" "POST" "" "Shorten content for all companies"

# ============================================================================
# INTERVIEW QUESTION GENERATION ENDPOINTS
# ============================================================================

echo "Testing Interview Question Generation Endpoints..." | tee -a $LOG_FILE

# Generate comprehensive interview questions for all skills
make_request "/interview-questions/generate-comprehensive" "POST" "" "Generate comprehensive interview questions for all skills"

# Generate comprehensive interview questions for specific skill
make_request "/interview-questions/generate-for-skill/Java" "POST" "" "Generate comprehensive interview questions for Java skill"

# Generate and save tech skills
make_request "/skills/generate-and-save" "POST" "" "Generate and save tech skills"

# Generate and save topics for specific skill
make_request "/skills/Java/topics/generate-and-save" "POST" "" "Generate and save topics for Java skill"

# Generate and save topics for all skills
make_request "/skills/topics/generate-and-save-all" "POST" "" "Generate and save topics for all skills"

# Generate and save questions for specific skill and topic
make_request "/skills/Java/topics/Collections/questions/generate-and-save?numQuestions=5" "POST" "" "Generate and save questions for Java Collections topic"

# Generate and save questions for all topics of a skill
make_request "/skills/Java/topics/questions/generate-and-save?numQuestions=5" "POST" "" "Generate and save questions for all topics of Java skill"

# Generate and save questions for all skills and topics
make_request "/skills/topics/questions/generate-and-save-all?numQuestions=5" "POST" "" "Generate and save questions for all skills and topics"

# ============================================================================
# SIMILAR CONTENT GENERATION ENDPOINTS
# ============================================================================

echo "Testing Similar Content Generation Endpoints..." | tee -a $LOG_FILE

# Generate similar designations for all
make_request "/designations/generate-similar" "POST" "" "Generate similar designations for all unprocessed designations"

# Generate similar companies for all
make_request "/companies/generate-similar" "POST" "" "Generate similar companies for all unprocessed companies"

# ============================================================================
# URL CONTENT MANAGEMENT ENDPOINTS
# ============================================================================

echo "Testing URL Content Management Endpoints..." | tee -a $LOG_FILE

# Get all URL content
make_request "/url-content" "GET" "" "Get all URL content"

# Get URL content by specific URL (URL encoded)
make_request "/url-content/https%3A%2F%2Fcareers.google.com%2Fjobs%2F123" "GET" "" "Get URL content for specific URL"

# ============================================================================
# CITY GENERATION ENDPOINTS
# ============================================================================

echo "Testing City Generation Endpoints..." | tee -a $LOG_FILE

# Reset cities processed flag for all countries
make_request "/cities/reset-processed-flag" "POST" "" "Reset cities processed flag for all countries"

# Generate cities for a specific country (using country ID 1)
make_request "/cities/generate-for-country/1" "POST" "" "Generate cities for specific country (ID: 1) using GenAI"

# Generate cities for all unprocessed countries
make_request "/cities/generate-for-countries" "POST" "" "Generate cities for all unprocessed countries using GenAI"

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