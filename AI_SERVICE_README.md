# AI Service Integration

This module provides AI-powered company information generation and extraction using Google Gemini AI model.

## Overview

The AI service system consists of two main components:

1. **GeminiService** - Handles Google Gemini AI API calls
2. **AIService** - Orchestrates GeminiService

## Features

### 🤖 **AI Model Support**
- **Primary**: Google Gemini AI (gemini-1.5-flash model)

### 📊 **Company Information Generation**
- Generate comprehensive company profiles from company names
- Extract detailed information from LinkedIn company pages
- Structured JSON output with all company attributes

### 🔄 **Rate Limit Handling**
- Automatic detection of rate limit errors
- Comprehensive logging for monitoring and debugging

## Configuration

### API Keys

Add the following to your `application.properties`:

```properties
# Gemini AI Configuration
gemini.api.key=your_gemini_api_key_here
```

### Default API Keys (Development)

The service includes a default API key for development:

- **Gemini**: `AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM`

⚠️ **Important**: Replace this with your own API key for production use.

## Usage

### Using the Combined AI Service

```java
@Autowired
private AIService aiService;

// Generate company info from company name
Optional<Company> company = aiService.generateCompanyInfo("Google", "https://linkedin.com/company/google");
```

### Using Individual Service

```java
@Autowired
private GeminiService geminiService;

// Use Gemini directly
Optional<Company> company = geminiService.generateCompanyInfo("Google", linkedinUrl);
```

## Fallback Logic

Gemini is the only AI model used. If Gemini fails or is rate-limited, the service logs the error and returns an empty result.

## Company Data Schema

The AI model returns company information in this JSON structure:

```json
{
  "name": "Company Name",
  "description": "Brief company description",
  "logo_url": "https://example.com/logo.png",
  "website": "https://www.company.com",
  "career_page_url": "https://careers.company.com",
  "about_us": "Detailed company description",
  "culture": "Company culture description",
  "mission": "Company mission statement",
  "vision": "Company vision statement",
  "company_size": "10,001+ employees",
  "headquarters": "Mountain View, CA, United States",
  "industry": "Technology",
  "linkedin_url": "https://linkedin.com/company/company",
  "specialties": "AI, Cloud Computing, Search",
  "is_crawled": true,
  "last_crawled_at": "2025-01-27T10:30:00Z"
}
```

## Logging

The service provides comprehensive logging:

- **Info Level**: Success/failure of API calls
- **Debug Level**: Request/response details
- **Warn Level**: Rate limits
- **Error Level**: API errors and exceptions

### Example Log Output

```
INFO  - Starting AI-powered company information generation for: Google
INFO  - Attempting to generate company info using Gemini AI for: Google
WARN  - Gemini AI hit rate limit for company: Google
ERROR - Failed to generate company info using Gemini AI for company: Google
```

## Performance Considerations

### Rate Limits

- **Gemini**: Varies by plan (typically 15 requests/minute for free tier)

### Response Times

- **Gemini**: ~2-5 seconds average

### Best Practices

1. **Use AIService**: Always use the AIService for company info generation
2. **Monitor Logs**: Watch for rate limit warnings
3. **Batch Processing**: Consider delays between requests for large batches
4. **Error Handling**: Always check for empty Optional results

## Troubleshooting

### Common Issues

1. **Rate Limit Errors**
   - Solution: The system logs the error and returns an empty result
   - Monitor logs for rate limit warnings

2. **API Key Issues**
   - Check that API keys are properly configured
   - Verify API keys have sufficient quota

3. **JSON Parsing Errors**
   - Check logs for malformed JSON responses
   - AI models occasionally return invalid JSON

4. **Network Timeouts**
   - Check network connectivity 