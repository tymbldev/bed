# Company Data Loader

This module provides functionality to load company data from pipe-separated text files into the database.

## Files Created

### 1. Data Files
- `src/main/resources/db/companies_basic.txt` - Contains basic company information (excluding aboutUs, vision, mission, culture)
- `src/main/resources/db/companies_detailed.txt` - Contains detailed company information (aboutUs, vision, mission, culture)

### 2. Java Classes
- `CompanyDataService.java` - Service class to handle file reading and database operations
- `CompanyDataController.java` - REST controller to expose endpoints

## API Endpoints

### 1. Load Basic Company Data
**Endpoint:** `POST /api/admin/company-data/load-basic`

**Description:** Reads `companies_basic.txt` and creates/updates company records in the database.

**Behavior:**
- Creates new company records if they don't exist
- Updates existing company records with null/empty fields from the file
- Includes: name, description, website, logo_url, headquarters, industry, company_size, specialties, linkedin_url, careers_url

**Response:** List of strings indicating the result of each company operation

### 2. Update Detailed Company Data
**Endpoint:** `POST /api/admin/company-data/update-detailed`

**Description:** Reads `companies_detailed.txt` and updates only null/empty detailed fields in existing company records. Uses company names to match records.

**Behavior:**
- Only updates companies that already exist in the database (matched by company name)
- Only updates fields that are currently null or empty
- Includes: aboutUs, vision, mission, culture

**Response:** List of strings indicating the result of each company operation

### 3. Cleanup Duplicate Companies
**Endpoint:** `POST /api/admin/company-data/cleanup-duplicates`

**Description:** Cleans up duplicate companies by name before loading new data. Removes duplicate entries while keeping the first occurrence of each company name.

**Behavior:**
- Finds all companies with duplicate names
- Keeps the first occurrence of each company name
- Removes all subsequent duplicates
- Returns detailed report of cleanup operations

**Response:** List of strings indicating the cleanup results

## File Format

### companies_basic.txt
```
id|name|description|website|logo_url|headquarters|industry|company_size|specialties|linkedin_url|careers_url
355|Infosys|Infosys is a global leader...|https://www.infosys.com|https://...|Bangalore, Karnataka|IT Services...|10,001+ employees|IT Solutions...|https://...|https://...
```

### companies_detailed.txt
```
companyName|aboutUs|vision|mission|culture
Infosys|Infosys is a global leader...|Be a globally respected...|To help enterprises...|A culture of innovation...
```

## Usage

1. **First, cleanup any existing duplicates:**
   ```bash
   curl -X POST http://localhost:8080/api/admin/company-data/cleanup-duplicates
   ```

2. **Then, load basic data:**
   ```bash
   curl -X POST http://localhost:8080/api/admin/company-data/load-basic
   ```

3. **Finally, update detailed data:**
   ```bash
   curl -X POST http://localhost:8080/api/admin/company-data/update-detailed
   ```

## Features

- **Safe Updates:** Only updates null/empty fields, preserving existing data
- **Error Handling:** Graceful handling of file reading errors and database issues
- **Transaction Support:** Database operations are wrapped in transactions
- **Detailed Logging:** Returns detailed results for each company operation
- **Logo URLs:** Includes company logo URLs from Wikimedia Commons
- **Duplicate Handling:** Automatically handles duplicate company names by updating existing records

## Data Included

The files contain data for 1000+ major companies including:
- Indian IT companies (Infosys, TCS, Wipro, HCLTech, etc.)
- Global tech companies (Amazon, Microsoft, Google, Apple, etc.)
- Indian banks (HDFC Bank, ICICI Bank, Axis Bank)
- Fintech companies (Razorpay, Paytm, PhonePe, etc.)
- Software companies (Zoho, Freshworks, Postman, etc.)
- Manufacturing companies (L&T, BHEL, Siemens, etc.)
- Automotive companies (Tata Motors, Mahindra, Maruti Suzuki, etc.)
- Healthcare companies (Sun Pharma, Dr. Reddy's, Cipla, etc.)

Each company includes comprehensive information with proper logo URLs and career page links.