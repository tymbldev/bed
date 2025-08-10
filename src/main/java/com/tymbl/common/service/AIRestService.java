package com.tymbl.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.AiDumper;
import com.tymbl.common.repository.AiDumperRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AIRestService {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    @Value("${gemini.api.key:NA}")
    private String geminiApiKey;

    @Value("${ai.service.connection.timeout:30000}")
    private int connectionTimeout;

    @Value("${ai.service.read.timeout:60000}")
    private int readTimeout;

    @Value("${ai.service.request.timeout:90000}")
    private int requestTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final AiDumperRepository aiDumperRepository;

    public AIRestService(AiDumperRepository aiDumperRepository) {
        this.aiDumperRepository = aiDumperRepository;
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        log.info("Configuring AI Service RestTemplate with timeouts - Connection: {}ms, Read: {}ms, Request: {}ms", 
                connectionTimeout, readTimeout, requestTimeout);
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add request timeout interceptor
        restTemplate.getInterceptors().add((request, body, execution) -> {
            long startTime = System.currentTimeMillis();
            try {
                org.springframework.http.client.ClientHttpResponse response = execution.execute(request, body);
                long duration = System.currentTimeMillis() - startTime;
                log.info("AI Service request completed in {}ms", duration);
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

    /**
     * Makes a centralized call to Gemini API with proper logging and error handling
     * @param requestBody The request body to send to Gemini API
     * @param operationName A descriptive name for the operation (for logging purposes)
     * @return ResponseEntity<String> containing the Gemini API response
     */
    public ResponseEntity<String> callGeminiAPI(Map<String, Object> requestBody, String operationName) {
        String url = GEMINI_API_URL + "?key=" + geminiApiKey;
        
        log.info("Making Gemini API call for operation: {}", operationName);
        log.info("Request URL: {}", GEMINI_API_URL + "?key=***");
        log.info("Request body: {}", requestBody);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        long startTime = System.currentTimeMillis();
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Gemini API call completed for operation: {} - Status: {} - Duration: {}ms", 
                    operationName, response.getStatusCodeValue(), duration);
            
            // Log to database
            try {
                logToDatabase(operationName, requestBody, response.getBody(), response.getStatusCodeValue() == 200);
            } catch (Exception e) {
                log.warn("Failed to log to database for operation: {}", operationName, e);
            }
            
            if (response.getStatusCodeValue() == 200) {
                log.info("Successful response from Gemini API for operation: {}", operationName);
                return response;
            } else {
                log.error("Gemini API error for operation: {} - Status: {} - Body: {}", 
                        operationName, response.getStatusCodeValue(), response.getBody());
                return response;
            }
            
        } catch (HttpClientErrorException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("HTTP error calling Gemini API for operation: {} - Duration: {}ms - Status: {} - Body: {}", 
                    operationName, duration, e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded for Gemini API. Operation: {}", operationName);
                throw new RuntimeException("Rate limit exceeded for Gemini API", e);
            }
            
            throw new RuntimeException("HTTP error calling Gemini API: " + e.getMessage(), e);
            
        } catch (ResourceAccessException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Connection error calling Gemini API for operation: {} - Duration: {}ms", 
                    operationName, duration, e);
            
            if (e.getCause() instanceof java.net.SocketTimeoutException) {
                log.error("Request timeout for Gemini API. Operation: {}", operationName, e);
                throw new RuntimeException("Request timeout for Gemini API", e);
            }
            
            throw new RuntimeException("Connection error calling Gemini API: " + e.getMessage(), e);
            
        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Runtime error calling Gemini API for operation: {} - Duration: {}ms", 
                    operationName, duration, e);
            
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                log.error("Request timeout for Gemini API. Operation: {}", operationName, e);
                throw e;
            }
            
            throw new RuntimeException("Runtime error calling Gemini API: " + e.getMessage(), e);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Unexpected error calling Gemini API for operation: {} - Duration: {}ms", 
                    operationName, duration, e);
            throw new RuntimeException("Unexpected error calling Gemini API: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a standard request body for Gemini API calls
     * @param prompt The prompt to send to Gemini
     * @return Map<String, Object> containing the formatted request body
     */
    public Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        contents.put("parts", parts);
        requestBody.put("contents", contents);
        
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 8192);
        requestBody.put("generationConfig", generationConfig);
        
        return requestBody;
    }

    /**
     * Get the configured Gemini API key (for backward compatibility)
     * @return The Gemini API key from configuration or default value
     */
    public String getGeminiApiKey() {
        return geminiApiKey;
    }

    /**
     * Get the Gemini API URL (for backward compatibility)
     * @return The Gemini API URL
     */
    public String getGeminiApiUrl() {
        return GEMINI_API_URL;
    }

    /**
     * Logs the Gemini API call details to the database
     * @param operationName The name of the operation
     * @param requestBody The request body sent to Gemini
     * @param responseBody The response body from Gemini
     * @param isSuccess Whether the call was successful
     */
    private void logToDatabase(String operationName, Map<String, Object> requestBody, String responseBody, boolean isSuccess) {
        try {
            AiDumper aiDumper = new AiDumper();
            aiDumper.setOperationName(operationName);
            try {
                aiDumper.setRequestBody(objectMapper.writeValueAsString(requestBody));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize request body for operation: {}", operationName, e);
                aiDumper.setRequestBody("Error serializing request body: " + e.getMessage());
            }
            aiDumper.setResponseBody(responseBody);
            
            // Parse response to extract metadata
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                try {
                    JsonNode responseNode = objectMapper.readTree(responseBody);
                    
                    // Extract model version
                    if (responseNode.has("modelVersion")) {
                        aiDumper.setModelVersion(responseNode.get("modelVersion").asText());
                    }
                    
                    // Extract usage metadata
                    if (responseNode.has("usageMetadata")) {
                        JsonNode usageMetadata = responseNode.get("usageMetadata");
                        if (usageMetadata.has("promptTokenCount")) {
                            aiDumper.setPromptTokenCount(usageMetadata.get("promptTokenCount").asInt());
                        }
                        if (usageMetadata.has("candidatesTokenCount")) {
                            aiDumper.setCandidatesTokenCount(usageMetadata.get("candidatesTokenCount").asInt());
                        }
                        if (usageMetadata.has("totalTokenCount")) {
                            aiDumper.setTotalTokenCount(usageMetadata.get("totalTokenCount").asInt());
                        }
                    }
                    
                    // Extract response ID
                    if (responseNode.has("responseId")) {
                        aiDumper.setResponseId(responseNode.get("responseId").asText());
                    }
                    
                    // Extract candidate information
                    if (responseNode.has("candidates") && responseNode.get("candidates").isArray() && 
                        responseNode.get("candidates").size() > 0) {
                        JsonNode candidate = responseNode.get("candidates").get(0);
                        if (candidate.has("finishReason")) {
                            aiDumper.setFinishReason(candidate.get("finishReason").asText());
                        }
                        if (candidate.has("avgLogprobs")) {
                            aiDumper.setAvgLogprobs(candidate.get("avgLogprobs").asDouble());
                        }
                    }
                    
                } catch (Exception e) {
                    log.warn("Failed to parse response metadata for operation: {}", operationName, e);
                }
            }
            
            aiDumperRepository.save(aiDumper);
            log.info("Successfully logged Gemini API call to database for operation: {}", operationName);
            
        } catch (Exception e) {
            log.error("Failed to log Gemini API call to database for operation: {}", operationName, e);
            throw e;
        }
    }
} 