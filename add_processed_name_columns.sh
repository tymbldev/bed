#!/bin/bash

# Script to add processed name columns to entities
# This script runs the migration to add processed_name and processed_name_generated columns

echo "=========================================="
echo "Adding Processed Name Columns to Entities"
echo "=========================================="
echo ""

# Check if MySQL is available
if ! command -v mysql &> /dev/null; then
    echo "Error: MySQL client is not installed or not in PATH"
    exit 1
fi

# Database configuration
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="tymbl"
DB_USER="root"
DB_PASS=""

# Check if database name is provided as argument
if [ $# -eq 1 ]; then
    DB_NAME=$1
    echo "Using database: $DB_NAME"
elif [ $# -eq 2 ]; then
    DB_NAME=$1
    DB_PASS=$2
    echo "Using database: $DB_NAME with password"
elif [ $# -eq 3 ]; then
    DB_HOST=$1
    DB_NAME=$2
    DB_PASS=$3
    echo "Using database: $DB_NAME on $DB_HOST with password"
elif [ $# -eq 4 ]; then
    DB_HOST=$1
    DB_PORT=$2
    DB_NAME=$3
    DB_PASS=$4
    echo "Using database: $DB_NAME on $DB_HOST:$DB_PORT with password"
fi

echo ""

# Migration file path
MIGRATION_FILE="src/main/resources/db/add_processed_name_columns.sql"

# Check if migration file exists
if [ ! -f "$MIGRATION_FILE" ]; then
    echo "Error: Migration file not found: $MIGRATION_FILE"
    exit 1
fi

echo "Running migration: $MIGRATION_FILE"
echo ""

# Run the migration
if [ -z "$DB_PASS" ]; then
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" < "$MIGRATION_FILE"
else
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$MIGRATION_FILE"
fi

# Check if migration was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migration completed successfully!"
    echo ""
    echo "Added columns to the following tables:"
    echo "  - countries: processed_name, processed_name_generated"
    echo "  - cities: processed_name, processed_name_generated"
    echo "  - companies: processed_name, processed_name_generated"
    echo "  - designations: processed_name, processed_name_generated"
    echo ""
    echo "Added indexes for better performance:"
    echo "  - Indexes on processed_name_generated for all tables"
    echo "  - Unique indexes on processed_name for all tables"
    echo ""
    echo "You can now use the processed name generation endpoints:"
    echo "  - POST /api/v1/ai/processed-names/generate-all"
    echo "  - POST /api/v1/ai/processed-names/generate-countries"
    echo "  - POST /api/v1/ai/processed-names/generate-cities"
    echo "  - POST /api/v1/ai/processed-names/generate-companies"
    echo "  - POST /api/v1/ai/processed-names/generate-designations"
    echo "  - POST /api/v1/ai/processed-names/reset"
else
    echo ""
    echo "❌ Migration failed!"
    echo "Please check the error messages above and try again."
    exit 1
fi 