#!/bin/bash

# Script to create secondary industry mapping table
# This script creates the secondary_industry_mapping table and inserts initial data

echo "=========================================="
echo "Creating Secondary Industry Mapping Table"
echo "=========================================="
echo ""

# Configuration - Update these values as needed
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"3306"}
DB_NAME=${DB_NAME:-"tymbl"}
DB_USER=${DB_USER:-"root"}
DB_PASSWORD=${DB_PASSWORD:-""}

# Check if database credentials are provided
if [ -z "$DB_PASSWORD" ]; then
    echo "⚠️  Warning: No database password provided. Using empty password."
    echo "   Set DB_PASSWORD environment variable if needed."
    echo ""
fi

# SQL file path
SQL_FILE="src/main/resources/db/create_secondary_industry_mapping_table.sql"

# Check if SQL file exists
if [ ! -f "$SQL_FILE" ]; then
    echo "❌ Error: SQL file not found: $SQL_FILE"
    exit 1
fi

echo "📁 SQL File: $SQL_FILE"
echo "🗄️  Database: $DB_NAME"
echo "🌐 Host: $DB_HOST:$DB_PORT"
echo "👤 User: $DB_USER"
echo ""

# Function to run SQL command
run_sql() {
    local sql="$1"
    if [ -z "$DB_PASSWORD" ]; then
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "$sql"
    else
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "$sql"
    fi
}

# Function to check if table exists
table_exists() {
    local result
    result=$(run_sql "SHOW TABLES LIKE 'secondary_industry_mapping';" 2>/dev/null | grep -c "secondary_industry_mapping" || echo "0")
    [ "$result" -gt 0 ]
}

# Check if table already exists
if table_exists; then
    echo "⚠️  Warning: Table 'secondary_industry_mapping' already exists."
    read -p "Do you want to drop and recreate it? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️  Dropping existing table..."
        run_sql "DROP TABLE IF EXISTS secondary_industry_mapping;"
        echo "✅ Table dropped successfully."
    else
        echo "❌ Operation cancelled. Table already exists."
        exit 0
    fi
fi

# Execute the SQL file
echo "🚀 Creating secondary industry mapping table..."
if [ -z "$DB_PASSWORD" ]; then
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" < "$SQL_FILE"
else
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$SQL_FILE"
fi

# Check if the operation was successful
if [ $? -eq 0 ]; then
    echo "✅ Secondary industry mapping table created successfully!"
    
    # Verify table creation
    echo ""
    echo "🔍 Verifying table structure..."
    run_sql "DESCRIBE secondary_industry_mapping;" 2>/dev/null
    
    echo ""
    echo "📊 Checking initial data..."
    run_sql "SELECT COUNT(*) as total_mappings FROM secondary_industry_mapping;" 2>/dev/null
    
    echo ""
    echo "🎯 Sample mappings:"
    run_sql "SELECT name, mapped_name, mapped_id FROM secondary_industry_mapping LIMIT 5;" 2>/dev/null
    
    echo ""
    echo "=========================================="
    echo "✅ Migration completed successfully!"
    echo "=========================================="
    echo ""
    echo "📋 Table Details:"
    echo "   - Table: secondary_industry_mapping"
    echo "   - Purpose: Maps secondary industry names to standardized parent categories"
    echo "   - Key Fields: name, mapped_id, mapped_name, processed"
    echo ""
    echo "🔗 Next Steps:"
    echo "   1. Start your Spring Boot application"
    echo "   2. Use the new API endpoints to process secondary industries"
    echo "   3. Monitor the mapping process through application logs"
    echo ""
else
    echo "❌ Error: Failed to create secondary industry mapping table."
    echo "   Please check your database connection and permissions."
    exit 1
fi 