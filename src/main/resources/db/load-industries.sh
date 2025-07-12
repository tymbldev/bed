#!/bin/bash

# Load Industries Data Script
# This script loads the industries data into the database

echo "🚀 Loading Industries Data..."

# Database configuration
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="tymbl"
DB_USER="root"
DB_PASSWORD=""

# Check if database name is provided as argument
if [ $# -eq 1 ]; then
    DB_NAME=$1
    echo "📊 Using database: $DB_NAME"
fi

# Check if database credentials are provided as environment variables
if [ ! -z "$DB_USER" ]; then
    echo "👤 Using database user: $DB_USER"
fi

if [ ! -z "$DB_PASSWORD" ]; then
    echo "🔐 Using database password: [HIDDEN]"
fi

# SQL file path
SQL_FILE="src/main/resources/db/industries.sql"

# Check if SQL file exists
if [ ! -f "$SQL_FILE" ]; then
    echo "❌ Error: SQL file not found at $SQL_FILE"
    exit 1
fi

echo "📁 SQL file found: $SQL_FILE"

# Execute SQL script
echo "⏳ Executing SQL script..."

if [ -z "$DB_PASSWORD" ]; then
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER $DB_NAME < $SQL_FILE
else
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASSWORD $DB_NAME < $SQL_FILE
fi

# Check if the command was successful
if [ $? -eq 0 ]; then
    echo "✅ Industries data loaded successfully!"
    echo "📊 Total industries loaded: $(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER $DB_NAME -e "SELECT COUNT(*) as total FROM industries;" -s -N)"
else
    echo "❌ Error: Failed to load industries data"
    exit 1
fi

echo "🎉 Industries data loading completed!" 