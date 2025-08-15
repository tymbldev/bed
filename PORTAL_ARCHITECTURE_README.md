# Portal-Based Job Crawling Architecture

## Overview

The job crawling system has been refactored to use a portal-based architecture that allows easy integration of multiple job portals (foundit, LinkedIn, Naukri, etc.) without modifying the core crawling logic.

## Architecture Components

### 1. PortalCrawlingService Interface

The core interface that all portal implementations must implement:

```java
public interface PortalCrawlingService {
    boolean canHandlePortal(String portalName);
    String makePortalApiCall(JobCrawlKeyword keywordConfig, JobCrawlRequest request);
    JobRawResponse saveRawResponse(JobCrawlKeyword keywordConfig, JobCrawlRequest request, String apiResponse);
    List<JobDetail> parseAndSaveJobDetails(JobRawResponse rawResponse, String apiResponse, JobCrawlRequest request);
    HttpHeaders getPortalHeaders(String portalName);
    String buildPortalApiUrl(JobCrawlKeyword keywordConfig, JobCrawlRequest request);
}
```

### 2. Portal Implementations

#### FounditPortalCrawlingService
- Handles foundit.in/monster.com API calls
- Parses the specific JSON response structure from foundit
- Handles nested structures for experience, salary, locations, etc.
- Supports Unix timestamp parsing for dates

#### LinkedInPortalCrawlingService
- Handles LinkedIn API calls (placeholder implementation)
- Demonstrates how to add new portals
- Can be extended with actual LinkedIn API integration

### 3. PortalCrawlingFactory

A factory service that routes requests to the appropriate portal implementation:

```java
@Service
public class PortalCrawlingFactory {
    public PortalCrawlingService getPortalService(String portalName);
    public boolean isPortalSupported(String portalName);
    public List<String> getSupportedPortals();
}
```

### 4. Updated JobCrawlingServiceImpl

The main service now delegates portal-specific operations to the appropriate portal service:

```java
// Get the appropriate portal service
PortalCrawlingService portalService = portalFactory.getPortalService(request.getPortalName());

// Make API call to the portal
String apiResponse = portalService.makePortalApiCall(keywordConfig, request);

// Save raw response
JobRawResponse rawResponse = portalService.saveRawResponse(keywordConfig, request, apiResponse);

// Parse and save job details
List<JobDetail> jobDetails = portalService.parseAndSaveJobDetails(rawResponse, apiResponse, request);
```

## Adding New Portals

To add a new job portal (e.g., Naukri):

1. **Create a new service class** implementing `PortalCrawlingService`:
   ```java
   @Service
   public class NaukriPortalCrawlingService implements PortalCrawlingService {
       // Implement all required methods
   }
   ```

2. **Update PortalCrawlingFactory** to recognize the new portal:
   ```java
   } else if (service instanceof com.tymbl.jobs.service.impl.NaukriPortalCrawlingService) {
       return "naukri";
   }
   ```

3. **Configure keywords** in the `job_crawl_keywords` table with the new portal name.

## Benefits of This Architecture

1. **Separation of Concerns**: Each portal has its own implementation for API calls, parsing, and data extraction.

2. **Easy Extension**: Adding new portals requires no changes to the core crawling logic.

3. **Portal-Specific Logic**: Each portal can handle its unique API structure, headers, and response format.

4. **Maintainability**: Portal-specific code is isolated and easier to maintain.

5. **Testing**: Each portal can be tested independently.

## Current Portal Support

- **foundit**: Full implementation with JSON parsing for the foundit.in API response structure
- **linkedin**: Placeholder implementation ready for LinkedIn API integration
- **Extensible**: Easy to add more portals like Naukri, Indeed, etc.

## Database Schema

The system uses the same database tables for all portals:
- `job_crawl_keywords`: Portal configuration and keywords
- `job_raw_responses`: Raw API responses from all portals
- `job_details`: Parsed job information from all portals

Each record includes a `portal_name` field to identify the source portal.

## Usage Example

```java
// The system automatically routes to the correct portal service
JobCrawlRequest request = new JobCrawlRequest();
request.setKeyword("software engineer");
request.setPortalName("foundit"); // or "linkedin", "naukri", etc.

JobCrawlResponse response = jobCrawlingService.crawlJobs(request);
```

## Future Enhancements

1. **Portal-Specific Configuration**: Add configuration files for each portal's API endpoints and parameters
2. **Rate Limiting**: Implement portal-specific rate limiting
3. **Error Handling**: Portal-specific error handling and retry logic
4. **Monitoring**: Portal-specific metrics and monitoring
5. **Caching**: Portal-specific response caching strategies
