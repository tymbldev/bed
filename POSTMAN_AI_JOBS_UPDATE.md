# Postman Collection Update: AI Job Controller

## Overview

The Postman collection has been updated to include all endpoints from the new `AIJobController` for job crawling functionality.

## New Section Added: "AI Job Crawling"

### 1. **Crawl Jobs** - `POST /api/v1/ai-jobs/crawl`
- **Purpose**: Crawl jobs for specific keyword and portal
- **Request Body**: 
  ```json
  {
    "keyword": "{{default_keyword}}",
    "portalName": "{{foundit_portal}}",
    "start": 0,
    "limit": 20,
    "countries": "{{default_country}}",
    "queryDerived": "true",
    "variantName": "embeddings512"
  }
  ```
- **Example Response**: Includes sample job data from foundit portal
- **Variables Used**: `{{default_keyword}}`, `{{foundit_portal}}`, `{{default_country}}`

### 2. **Manual Crawl by Keyword and Portal** - `POST /api/v1/ai-jobs/crawl/{keyword}/{portalName}`
- **Purpose**: Crawl jobs using path parameters
- **URL**: `{{base_url}}/api/v1/ai-jobs/crawl/{{default_keyword}}/{{foundit_portal}}`
- **Variables Used**: `{{default_keyword}}`, `{{foundit_portal}}`

### 3. **Crawl All Active Keywords** - `POST /api/v1/ai-jobs/crawl-all`
- **Purpose**: Automatically crawl all active keywords for all configured portals
- **No request body required**

### 4. **Process Pending Responses** - `POST /api/v1/ai-jobs/process-pending`
- **Purpose**: Process any pending raw API responses and extract job details
- **No request body required**

### 5. **Get Crawling Statistics** - `GET /api/v1/ai-jobs/stats`
- **Purpose**: Returns statistics about the crawling process
- **Example Response**: 
  ```json
  {
    "status": "SUCCESS",
    "message": "Crawling statistics retrieved successfully",
    "totalJobsFound": 1250,
    "rawResponseId": 45,
    "processedJobsCount": 1250
  }
  ```

### 6. **Get All Keywords** - `GET /api/v1/ai-jobs/keywords`
- **Purpose**: Returns all configured keywords and portal configurations
- **Example Response**: 
  ```json
  [
    "Keywords endpoint - implementation needed"
  ]
  ```

### 7. **Add New Keyword** - `POST /api/v1/ai-jobs/keywords/add`
- **Purpose**: Adds a new keyword and portal configuration for crawling
- **Request Body**:
  ```json
  {
    "keyword": "data scientist",
    "portalName": "{{linkedin_portal}}",
    "start": 0,
    "limit": 20,
    "countries": "United States",
    "queryDerived": "true",
    "variantName": "embeddings512"
  }
  ```
- **Variables Used**: `{{linkedin_portal}}`

## New Environment Variables Added

The collection now includes these additional variables for easy configuration:

```json
{
  "key": "foundit_portal",
  "value": "foundit",
  "type": "string"
},
{
  "key": "linkedin_portal", 
  "value": "linkedin",
  "type": "string"
},
{
  "key": "default_keyword",
  "value": "software engineer", 
  "type": "string"
},
{
  "key": "default_country",
  "value": "India",
  "type": "string"
}
```

## Portal Support

The endpoints support multiple job portals:
- **foundit**: Full implementation for foundit.in/monster.com
- **linkedin**: Ready for LinkedIn API integration
- **Extensible**: Easy to add more portals like Naukri, Indeed, etc.

## Usage Examples

### Crawl Jobs from Foundit
```bash
POST {{base_url}}/api/v1/ai-jobs/crawl
Content-Type: application/json

{
  "keyword": "software engineer",
  "portalName": "foundit",
  "start": 0,
  "limit": 20,
  "countries": "India"
}
```

### Manual Crawl
```bash
POST {{base_url}}/api/v1/ai-jobs/crawl/software%20engineer/foundit
```

### Get Statistics
```bash
GET {{base_url}}/api/v1/ai-jobs/stats
```

## Benefits

1. **Easy Testing**: All AI job crawling endpoints are now available in Postman
2. **Variable Support**: Uses environment variables for easy configuration
3. **Example Responses**: Includes sample responses for better understanding
4. **Portal Flexibility**: Supports multiple job portals with the same endpoints
5. **Complete Coverage**: All controller methods are included

## Next Steps

1. **Import the updated collection** into Postman
2. **Set up environment variables** for your specific configuration
3. **Test the endpoints** with your actual API
4. **Customize request bodies** as needed for your use case
5. **Add more portals** by extending the portal implementations
