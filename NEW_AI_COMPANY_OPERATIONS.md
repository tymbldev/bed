# New AI Company Operations - Logo and Website Fetching

## Overview
This document describes the new AI-powered operations added to the Tymbl API for fetching company logos and websites using GenAI.

## New Operations

### 1. Logo Fetching (`fetchLogos`)
- **Purpose**: Fetches official company logos using AI
- **Uses**: GenAI to find direct download URLs to company logos
- **Storage**: Saves logo URLs in the `logoUrl` field
- **Status Tracking**: Uses `logoUrlFetched` flag (0=not tried, 1=fetched, 2=failed)

### 2. Website Fetching (`fetchWebsites`)
- **Purpose**: Fetches official company websites using AI
- **Uses**: GenAI to find official corporate website URLs
- **Storage**: Saves website URLs in the `website` field
- **Status Tracking**: Uses `websiteFetched` flag (0=not tried, 1=fetched, 2=failed)

## API Endpoints

### Base Endpoint
```
POST /api/v1/ai/companies/process
```

### Available Operations
- `crawl` - Company crawling
- `detectIndustries` - Industry detection
- `shortenContent` - Content shortening
- `similarCompanies` - Similar companies generation
- `cleanup` - Company cleanup
- `shortname` - Shortname generation
- `fetchLogos` - **NEW** Logo fetching
- `fetchWebsites` - **NEW** Website fetching

## Usage Examples

### 1. Fetch Logos for All Companies
```bash
POST /api/v1/ai/companies/process?operations=fetchLogos
```

### 2. Fetch Logo for Specific Company
```bash
POST /api/v1/ai/companies/process?operations=fetchLogos&companyName=Google
```

### 3. Fetch Websites for All Companies
```bash
POST /api/v1/ai/companies/process?operations=fetchWebsites
```

### 4. Fetch Website for Specific Company
```bash
POST /api/v1/ai/companies/process?operations=fetchWebsites&companyName=Google
```

### 5. Combined Operations
```bash
# Fetch both logos and websites for all companies
POST /api/v1/ai/companies/process?operations=fetchLogos,fetchWebsites

# Fetch logos and websites for specific company
POST /api/v1/ai/companies/process?operations=fetchLogos,fetchWebsites&companyName=Google

# Process all operations including new ones
POST /api/v1/ai/companies/process
```

## Reset Flags

### Reset Logo Flags
```bash
# Reset for all companies
POST /api/v1/ai/companies/reset-flags?flags=logoUrlFetched

# Reset for specific company
POST /api/v1/ai/companies/reset-flags?flags=logoUrlFetched&companyName=Google
```

### Reset Website Flags
```bash
# Reset for all companies
POST /api/v1/ai/companies/reset-flags?flags=websiteFetched

# Reset for specific company
POST /api/v1/ai/companies/reset-flags?flags=websiteFetched&companyName=Google
```

### Reset All Flags (Including New Ones)
```bash
POST /api/v1/ai/companies/reset-flags?flags=is_crawled,industry_processed,content_shortened,similar_companies_processed,cleanup_processed,shortname_generated,logoUrlFetched,websiteFetched
```

## Response Format

### Success Response
```json
{
  "message": "Company processing completed",
  "companyName": "Google",
  "requestedOperations": ["fetchLogos", "fetchWebsites"],
  "operations": {
    "fetchLogos": {
      "processed": true,
      "message": "Logo fetched successfully",
      "result": {
        "success": true,
        "companyId": 123,
        "companyName": "Google",
        "logoUrl": "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png",
        "message": "Logo URL fetched successfully"
      }
    },
    "fetchWebsites": {
      "processed": true,
      "message": "Website fetched successfully",
      "result": {
        "success": true,
        "companyId": 123,
        "companyName": "Google",
        "website": "https://www.google.com",
        "message": "Website URL fetched successfully"
      }
    }
  },
  "status": "SUCCESS"
}
```

### Error Response
```json
{
  "error": "Error fetching logo: Failed to generate logo URL",
  "status": "ERROR"
}
```

## AI Prompt Design

### Logo Fetching Prompt
- Requests direct download URLs to official logos
- Prefers high-resolution PNG or SVG formats
- Uses official company websites, CDNs, or reliable hosting services
- Ensures URLs are publicly accessible and downloadable

### Website Fetching Prompt
- Requests official website URLs (not social media, job boards, etc.)
- Prefers main corporate websites with https:// protocol
- Ensures URLs are currently active and accessible
- Uses most commonly known and used website URLs

## Database Schema

### Company Table Fields
- `logoUrl` (VARCHAR) - Stores the fetched logo URL
- `logoUrlFetched` (INTEGER) - Status flag (0=not tried, 1=fetched, 2=failed)
- `website` (VARCHAR) - Stores the fetched website URL
- `websiteFetched` (INTEGER) - Status flag (0=not tried, 1=fetched, 2=failed)

## Features

### Memory Efficient
- Uses database pagination instead of loading all companies into memory
- Processes companies in batches of 10

### Smart Deduplication
- Skips companies that already have logos/websites fetched
- Checks `logoUrlFetched` and `websiteFetched` flags

### Error Handling
- Marks failed attempts with status 2
- Provides detailed error messages
- Continues processing other companies even if some fail

### Scalable
- Can handle large datasets without memory issues
- Database-based batching for efficient processing

## Postman Collection Updates

The Postman collection has been updated with the following new requests:

1. **Fetch Logos for All Companies**
2. **Fetch Logo for Specific Company**
3. **Fetch Websites for All Companies**
4. **Fetch Website for Specific Company**
5. **Reset Logo Flags (All Companies)**
6. **Reset Website Flags (All Companies)**

All existing requests have also been updated to include the new operations in their examples. 