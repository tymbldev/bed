#!/bin/bash

# Script to load dropdown data into MySQL database using hardcoded credentials

DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="job_referral_db"
DB_USER="root"
DB_PASS="Tymbl@123"

MYSQL_CMD="mysql -h $DB_HOST -P $DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME"

echo "Loading dropdown data into MySQL database $DB_NAME as user $DB_USER"

# Create the database if it doesn't exist
echo "Creating database if not exists..."
mysql -h $DB_HOST -P $DB_PORT -u$DB_USER -p$DB_PASS -e "CREATE DATABASE IF NOT EXISTS $DB_NAME;"
if [ $? -ne 0 ]; then
    echo "Error creating database $DB_NAME"
    exit 1
fi

# Load departments
echo "Loading departments..."
$MYSQL_CMD < ./departments.sql
if [ $? -eq 0 ]; then
    echo "Departments loaded successfully."
else
    echo "Error loading departments."
    exit 1
fi

# Load designations
echo "Loading designations..."
$MYSQL_CMD < ./designations.sql
if [ $? -eq 0 ]; then
    echo "Designations loaded successfully."
else
    echo "Error loading designations."
    exit 1
fi

# Load locations
echo "Loading locations..."
$MYSQL_CMD < ./locations.sql
if [ $? -eq 0 ]; then
    echo "Locations loaded successfully."
else
    echo "Error loading locations."
    exit 1
fi

echo "All dropdown data loaded successfully!"
