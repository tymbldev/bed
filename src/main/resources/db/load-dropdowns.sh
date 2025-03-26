#!/bin/bash

# Script to load dropdown data into the database
# Usage: ./load-dropdowns.sh [database_url] [username] [password]

DB_URL=${1:-"jdbc:postgresql://localhost:5432/tymbl"}
DB_USER=${2:-"postgres"}
DB_PASS=${3:-"postgres"}

echo "Loading dropdown data into database $DB_URL as user $DB_USER"

# Load departments
echo "Loading departments..."
psql "$DB_URL" -U "$DB_USER" -f ./departments.sql
if [ $? -eq 0 ]; then
    echo "Departments loaded successfully."
else
    echo "Error loading departments."
    exit 1
fi

# Load designations
echo "Loading designations..."
psql "$DB_URL" -U "$DB_USER" -f ./designations.sql
if [ $? -eq 0 ]; then
    echo "Designations loaded successfully."
else
    echo "Error loading designations."
    exit 1
fi

# Load locations
echo "Loading locations..."
psql "$DB_URL" -U "$DB_USER" -f ./locations.sql
if [ $? -eq 0 ]; then
    echo "Locations loaded successfully."
else
    echo "Error loading locations."
    exit 1
fi

echo "All dropdown data loaded successfully!" 