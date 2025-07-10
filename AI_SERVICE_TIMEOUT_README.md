# AI Service Timeout Implementation

This document describes the timeout implementation for the Grok and Gemini AI services in the Tymbl application.

## Overview

The AI services (Grok and Gemini) now include comprehensive timeout handling to prevent long-running requests from blocking the application and to provide better user experience.

## Configuration

### Timeout Properties

The following timeout configurations are available in `application.properties`:

```properties
# AI Service Timeout Configuration (in milliseconds)
ai.service.connection.timeout=30000    # 30 seconds - Time to establish connection
ai.service.read.timeout=60000          # 60 seconds - Time to read response
ai.service.request.timeout=90000       # 90 seconds - Total request timeout
```

### Default Values

- **Connection Timeout**: 30 seconds
- **Read Timeout**: 60 seconds  
- **Request Timeout**: 90 seconds

## Implementation Details

### 1. AIServiceConfig

A new configuration class `AIServiceConfig` creates a custom `RestTemplate` with timeout settings:

```java
@Configuration
public class AIServiceConfig {
    
    @Bean
    public RestTemplate aiServiceRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add request timeout interceptor for monitoring
        restTemplate.getInterceptors().add((request, body, execution) -> {
            long startTime = System.currentTimeMillis();
            try {
                var response = execution.execute(request, body);
                long duration = System.currentTimeMillis() - startTime;
                log.debug("AI Service request completed in {}ms", duration);
                return response;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                if (duration >= requestTimeout) {
                    log.error("AI Service request timed out after {}ms", duration);
                    throw new RuntimeException("Request timeout after " + duration + "ms", e);
                }
                throw e;
            }
        });
        
        return restTemplate;
    }
}
```

### 2. Timeout Handling in Services

Both `GeminiService` now handles multiple types of timeout exceptions:

#### Exception Types Handled

1. **ResourceAccessException** - Network-level timeouts
2. **SocketTimeoutException** - Socket read/write timeouts
3. **RuntimeException** - Custom timeout exceptions from interceptor

#### Error Handling Pattern

```java
try {
    ResponseEntity<String> response = restTemplate.exchange(...);
    // Process successful response
} catch (HttpClientErrorException e) {
    // Handle HTTP errors (rate limits, etc.)
} catch (ResourceAccessException e) {
    if (e.getCause() instanceof java.net.SocketTimeoutException) {
        log.error("Request timeout for API. Company: {}", companyName, e);
        throw new RuntimeException("Request timeout for API", e);
    } else {
        log.error("Connection error calling API for company: {}", companyName, e);
        return Optional.empty();
    }
} catch (RuntimeException e) {
    if (e.getMessage() != null && e.getMessage().contains("timeout")) {
        log.error("Request timeout for API. Company: {}", companyName, e);
        throw e;
    } else {
        log.error("Runtime error calling API for company: {}", companyName, e);
        return Optional.empty();
    }
}
```

### 3. Fallback Logic

The `AIService` orchestrates both AI models with intelligent fallback:

```java
// Try Gemini first
try {
    Optional<Company> geminiResult = geminiService.generateCompanyInfo(companyName, linkedinUrl);
    if (geminiResult.isPresent()) {
        return geminiResult;
    }
} catch (RuntimeException e) {
    if (e.getMessage() != null && e.getMessage().contains("Rate limit exceeded")) {
        log.warn("Gemini AI hit rate limit, falling back to Grok AI");
    } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
        log.warn("Gemini AI request timed out, falling back to Grok AI");
    }
}

// Fallback to Grok
try {
    Optional<Company> grokResult = grokService.generateCompanyInfo(companyName, linkedinUrl);
    if (grokResult.isPresent()) {
        return grokResult;
    }
} catch (RuntimeException e) {
    if (e.getMessage() != null && e.getMessage().contains("timeout")) {
        log.error("Both Gemini and Grok AI timed out");
    }
}
```

## Timeout Scenarios

### 1. Connection Timeout (30s)
- **When**: Network issues, DNS resolution problems, or server unavailability
- **Behavior**: Throws `ResourceAccessException` with connection timeout
- **Fallback**: Automatic fallback to alternative AI service

### 2. Read Timeout (60s)
- **When**: AI service takes too long to generate response
- **Behavior**: Throws `ResourceAccessException` with socket timeout
- **Fallback**: Automatic fallback to alternative AI service

### 3. Request Timeout (90s)
- **When**: Total request time exceeds configured limit
- **Behavior**: Custom timeout exception from interceptor
- **Fallback**: Automatic fallback to alternative AI service

## Logging

### Timeout Log Messages

```
ERROR - Request timeout for Gemini API. Company: Google
ERROR - Request timeout for Grok API. Company: Google
WARN  - Gemini AI request timed out, falling back to Grok AI for company: Google
ERROR - Both Gemini and Grok AI timed out for company: Google
```

### Performance Monitoring

```
DEBUG - AI Service request completed in 2500ms
ERROR - AI Service request timed out after 90000ms
```

## Best Practices

### 1. Configuration

- **Production**: Use conservative timeout values (30s connection, 60s read, 90s total)
- **Development**: Can use shorter timeouts for faster feedback
- **High Load**: Consider increasing timeouts during peak usage

### 2. Monitoring

- Monitor timeout frequency in logs
- Set up alerts for high timeout rates
- Track fallback usage between AI services

### 3. Error Handling

- Always check for empty `Optional` results
- Implement retry logic for transient failures
- Consider circuit breaker pattern for persistent failures

## Troubleshooting

### Common Issues

1. **Frequent Timeouts**
   - Check network connectivity
   - Verify AI service API status
   - Consider increasing timeout values

2. **Both Services Timing Out**
   - Check overall system load
   - Verify API key validity
   - Monitor AI service rate limits

3. **Performance Degradation**
   - Review timeout configuration
   - Check for memory leaks
   - Monitor system resources

### Debug Mode

Enable debug logging for detailed timeout information:

```properties
logging.level.com.tymbl.common.service=DEBUG
logging.level.com.tymbl.config=DEBUG
```

## Future Enhancements

1. **Dynamic Timeout Adjustment**: Adjust timeouts based on historical performance
2. **Circuit Breaker**: Implement circuit breaker pattern for persistent failures
3. **Retry Logic**: Add exponential backoff retry mechanism
4. **Metrics**: Add Prometheus metrics for timeout monitoring
5. **Health Checks**: Implement AI service health check endpoints 