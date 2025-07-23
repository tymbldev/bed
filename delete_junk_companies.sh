#!/bin/bash

# Script to delete junk-marked companies after manual review
# WARNING: This will permanently delete companies marked as junk

echo "=========================================="
echo "Junk Company Deletion Script"
echo "=========================================="
echo ""

# Configuration
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="tymbl_db"
DB_USER="postgres"

# Check if database credentials are provided
if [ $# -eq 4 ]; then
    DB_HOST=$1
    DB_PORT=$2
    DB_NAME=$3
    DB_USER=$4
    echo "Using database: $DB_USER@$DB_HOST:$DB_PORT/$DB_NAME"
elif [ $# -eq 1 ]; then
    DB_PASSWORD=$1
    echo "Using default database with provided password"
else
    echo "Usage: $0 [host] [port] [database] [user] [password]"
    echo "   or: $0 [password] (for default localhost:5432/tymbl_db/postgres)"
    echo ""
    echo "Example:"
    echo "  $0 localhost 5432 tymbl_db postgres mypassword"
    echo "  $0 mypassword"
    exit 1
fi

# Get password
if [ $# -eq 5 ]; then
    DB_PASSWORD=$5
elif [ $# -eq 1 ]; then
    DB_PASSWORD=$1
else
    echo -n "Enter database password: "
    read -s DB_PASSWORD
    echo ""
fi

echo ""
echo "⚠️  WARNING: This script will permanently delete companies marked as junk!"
echo ""
echo "Before proceeding, please:"
echo "1. Review junk-marked companies using the API endpoint:"
echo "   GET /api/v1/ai/companies/cleanup/junk-marked"
echo "2. Verify the companies you want to delete"
echo "3. Make sure you have a backup of your database"
echo ""

echo -n "Do you want to proceed with deletion? (y/N): "
read -r confirm

if [[ ! $confirm =~ ^[Yy]$ ]]; then
    echo "Operation cancelled."
    exit 0
fi

echo ""
echo "First, let's see what companies are marked as junk..."

# Show junk companies
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT 
    id,
    name,
    junk_reason,
    parent_company_name,
    cleanup_processed_at
FROM companies 
WHERE is_junk = true
ORDER BY cleanup_processed_at DESC;
"

echo ""
echo -n "Do you want to delete ALL junk-marked companies? (y/N): "
read -r delete_all

if [[ $delete_all =~ ^[Yy]$ ]]; then
    echo ""
    echo "Deleting all junk-marked companies..."
    
    # Delete all junk companies
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "DELETE FROM companies WHERE is_junk = true;"
    
    if [ $? -eq 0 ]; then
        echo "✅ Successfully deleted all junk-marked companies!"
    else
        echo "❌ Error deleting junk companies!"
        exit 1
    fi
else
    echo ""
    echo "To delete specific companies, you can:"
    echo "1. Edit the delete_junk_companies.sql file"
    echo "2. Uncomment the specific DELETE statements you want"
    echo "3. Run: PGPASSWORD=your_password psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f src/main/resources/db/delete_junk_companies.sql"
    echo ""
    echo "Or use the API endpoint to clear junk flags without deleting:"
    echo "POST /api/v1/ai/companies/cleanup/clear-junk-flag/{companyId}"
fi

echo ""
echo "Final statistics:"
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT 
    COUNT(*) as total_companies,
    COUNT(CASE WHEN is_junk = true THEN 1 END) as junk_companies,
    ROUND((COUNT(CASE WHEN is_junk = true THEN 1 END) * 100.0 / COUNT(*)), 2) as junk_percentage
FROM companies;
" 