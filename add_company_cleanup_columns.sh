#!/bin/bash

# Script to add company cleanup columns to the database
# This script adds columns needed for company cleanup functionality

echo "=========================================="
echo "Adding Company Cleanup Columns"
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
echo "This script will add the following columns to the companies table:"
echo "  - shortname (VARCHAR(255))"
echo "  - cleanup_processed (BOOLEAN DEFAULT FALSE)"
echo "  - cleanup_processed_at (TIMESTAMP)"
echo "  - Index on cleanup_processed for better performance"
echo ""

echo -n "Do you want to proceed? (y/N): "
read -r confirm

if [[ ! $confirm =~ ^[Yy]$ ]]; then
    echo "Operation cancelled."
    exit 0
fi

echo ""
echo "Executing SQL script..."

# Execute the SQL script
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f src/main/resources/db/add_company_cleanup_columns.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Company cleanup columns added successfully!"
    echo ""
    echo "New columns added:"
    echo "  - shortname: For storing company shortnames (e.g., Zomato for Eternal)"
    echo "  - cleanup_processed: Flag to track cleanup processing status"
    echo "  - cleanup_processed_at: Timestamp of cleanup processing"
    echo ""
    echo "Next steps:"
    echo "  1. Restart your application"
    echo "  2. Test the new endpoints:"
    echo "     - POST /api/v1/ai/companies/cleanup-all"
    echo "     - POST /api/v1/ai/companies/cleanup/{companyName}"
    echo "     - POST /api/v1/ai/companies/cleanup/reset-processed-flag"
    echo ""
else
    echo ""
    echo "❌ Error adding company cleanup columns!"
    echo "Please check your database connection and try again."
    exit 1
fi 