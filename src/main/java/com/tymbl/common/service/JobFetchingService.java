package com.tymbl.common.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.UrlContent;
import com.tymbl.common.repository.UrlContentRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobFetchingService {

    private final WebScrapingService webScrapingService;
    private final UrlContentRepository urlContentRepository;
    @Qualifier("aiServiceRestTemplate")
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";

    /**
     * Fetch jobs for a company using the updated two-step workflow
     * @param companyName The name of the company
     * @return List of job data
     */
    public List<Map<String, Object>> fetchJobsForCompany(String companyName) {
        try {
            log.info("Starting job fetching for company: {}", companyName);
            
            // Step 1: Get basic job details for all jobs
            List<Map<String, Object>> basicJobs = fetchBasicJobDetails(companyName);
            
            // Step 2: Enhance each job with detailed information
            List<Map<String, Object>> detailedJobs = enhanceJobsWithDetails(basicJobs);
            
            // Step 3: Process and enhance jobs with URL scraping based on conditions
            return processAndEnhanceJobs(detailedJobs);
            
        } catch (Exception e) {
            log.error("Error fetching jobs for company: {}", companyName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Step 1: Fetch basic job details for all jobs
     */
    private List<Map<String, Object>> fetchBasicJobDetails(String companyName) {
        try {
            log.info("Step 1: Fetching basic job details for company: {}", companyName);
            
            String prompt = buildBasicJobFetchingPrompt(companyName);
            List<Map<String, Object>> jobs = callGeminiAPI(prompt);
            
            log.info("Step 1 completed: Found {} basic jobs for company: {}", jobs.size(), companyName);
            return jobs;
            
        } catch (Exception e) {
            log.error("Error fetching basic job details for company: {}", companyName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Step 2: Enhance each job with detailed information
     */
    private List<Map<String, Object>> enhanceJobsWithDetails(List<Map<String, Object>> basicJobs) {
        List<Map<String, Object>> detailedJobs = new ArrayList<>();
        
        for (Map<String, Object> basicJob : basicJobs) {
            try {
                String jobTitle = (String) basicJob.get("title");
                String applyUrl = (String) basicJob.get("apply_url");
                
                if (jobTitle != null && applyUrl != null) {
                    log.info("Step 2: Enhancing job details for: {}", jobTitle);
                    
                    // Get detailed information for this specific job
                    Map<String, Object> detailedJob = fetchDetailedJobInfo(jobTitle, applyUrl);
                    
                    // Merge basic and detailed information
                    Map<String, Object> mergedJob = new HashMap<>(basicJob);
                    mergedJob.putAll(detailedJob);
                    detailedJobs.add(mergedJob);
                    
                    // Add delay between requests to avoid rate limiting
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted during delay between job enhancement requests", e);
                    }
                } else {
                    detailedJobs.add(basicJob);
                }
                
            } catch (Exception e) {
                log.error("Error enhancing job details for job: {}", basicJob, e);
                detailedJobs.add(basicJob); // Add original job if enhancement fails
            }
        }
        
        log.info("Step 2 completed: Enhanced {} jobs with detailed information", detailedJobs.size());
        return detailedJobs;
    }

    /**
     * Fetch detailed information for a specific job
     */
    private Map<String, Object> fetchDetailedJobInfo(String jobTitle, String applyUrl) {
        try {
            String prompt = buildDetailedJobPrompt(jobTitle, applyUrl);
            List<Map<String, Object>> detailedJobs = callGeminiAPI(prompt);
            
            if (!detailedJobs.isEmpty()) {
                return detailedJobs.get(0);
            }
            
        } catch (Exception e) {
            log.error("Error fetching detailed job info for: {}", jobTitle, e);
        }
        
        return new HashMap<>();
    }

    /**
     * Build the basic job fetching prompt (Step 1)
     */
    private String buildBasicJobFetchingPrompt(String companyName) {
        return String.format(
            "Fetch only recent and authentic job listings for the company '%s'. Use real-time, verifiable data from multiple trusted sources such as:\n\n" +
            "The company's official careers page\n" +
            "LinkedIn\n" +
            "Naukri.com\n" +
            "Greenhouse, Lever, or other ATS pages\n" +
            "Any other legitimate job portal with reliable listings\n\n" +
            "Do NOT guess or fabricate any job. Only include roles that are publicly available right now. Return data strictly as a JSON array in the format below:\n\n" +
            "[\n" +
            "  {\n" +
            "    \"title\": \"...\",                     // Job title\n" +
            "    \"designation\": \"...\",               // Designation name (not ID)\n" +
            "    \"description\": \"...\",               // Brief job description or summary\n" +
            "    \"location\": {\n" +
            "      \"city\": \"...\",                    // City name\n" +
            "      \"country\": \"...\"                  // Country name\n" +
            "    },\n" +
            "    \"job_type\": \"...\",                  // One of: \"Remote Only\", \"Onsite\", \"Hybrid\", \"Work From Office\"\n" +
            "    \"salary\": {\n" +
            "      \"min\": ...,                       // Numeric value\n" +
            "      \"max\": ...,                       // Numeric value\n" +
            "      \"currency\": \"...\"                 // Currency code (e.g., USD, INR)\n" +
            "    },\n" +
            "    \"experience\": {\n" +
            "      \"min\": ...,                       // Years\n" +
            "      \"max\": ...\n" +
            "    },\n" +
            "    \"skills\": [\"...\", \"...\"],           // Required skills (names, not IDs)\n" +
            "    \"tags\": [\"...\", \"...\"],             // Any relevant job tags or keywords\n" +
            "    \"openings\": ...,                    // Number of openings\n" +
            "    \"posted\": \"...\",                    // ISO date (e.g., \"2025-07-15\") or relative age (e.g., \"3 days ago\")\n" +
            "    \"platform\": \"...\",                  // Source portal (e.g., \"LinkedIn\", \"Naukri\", \"Company Website\")\n" +
            "    \"apply_url\": \"...\"                  // Direct link to apply\n" +
            "  }\n" +
            "]\n\n" +
            "IMPORTANT: Return ONLY the JSON array. Do not include any explanations, comments, or additional text before or after the array.",
            companyName
        );
    }

    /**
     * Build the detailed job fetching prompt (Step 2)
     */
    private String buildDetailedJobPrompt(String jobTitle, String applyUrl) {
        return String.format(
            "Get detailed information for the job '%s' from the URL '%s'. Extract comprehensive details including:\n\n" +
            "1. Detailed job description with responsibilities\n" +
            "2. Required qualifications and experience\n" +
            "3. Technical skills and technologies\n" +
            "4. Benefits and perks\n" +
            "5. Company culture information\n" +
            "6. Application process details\n" +
            "7. Any additional requirements or preferences\n\n" +
            "Return the information in JSON format:\n\n" +
            "{\n" +
            "  \"detailed_description\": \"...\",        // Comprehensive job description\n" +
            "  \"responsibilities\": [\"...\", \"...\"],   // List of key responsibilities\n" +
            "  \"requirements\": [\"...\", \"...\"],       // List of requirements\n" +
            "  \"benefits\": [\"...\", \"...\"],           // List of benefits and perks\n" +
            "  \"application_process\": \"...\",          // Application process details\n" +
            "  \"additional_info\": \"...\"               // Any additional information\n" +
            "}\n\n" +
            "IMPORTANT: Return ONLY the JSON object. Do not include any explanations, comments, or additional text before or after the object.",
            jobTitle, applyUrl
        );
    }

    /**
     * Call Gemini API with the prompt
     */
    private List<Map<String, Object>> callGeminiAPI(String prompt) {
        try {
            log.info("Calling Gemini API for job fetching");
            
            Map<String, Object> requestBody = buildRequestBody(prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            
            // Log raw Gemini response
            log.info("Gemini API Raw Response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseGeminiResponse(response.getBody());
            } else {
                log.error("Gemini API error: {} - {}", response.getStatusCode(), response.getBody());
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return new ArrayList<>();
        }
    }

    /**
     * Parse Gemini API response
     */
    private List<Map<String, Object>> parseGeminiResponse(String responseBody) {
        try {
            // Extract the text content from Gemini response
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
            
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                
                if (parts != null && !parts.isEmpty()) {
                    String text = (String) parts.get(0).get("text");
                    if (text != null) {
                        // Try to extract JSON from the response
                        return extractJsonFromText(text);
                    }
                }
            }
            
            log.warn("Could not parse Gemini response");
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            return new ArrayList<>();
        }
    }

    /**
     * Extract JSON array from text response
     */
    private List<Map<String, Object>> extractJsonFromText(String text) {
        try {
            // Find JSON array in the text
            int startIndex = text.indexOf('[');
            int endIndex = text.lastIndexOf(']');
            
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String jsonText = text.substring(startIndex, endIndex + 1);
                return objectMapper.readValue(jsonText, new TypeReference<List<Map<String, Object>>>() {});
            }
            
            log.warn("No JSON array found in response text");
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error extracting JSON from text", e);
            return new ArrayList<>();
        }
    }

    /**
     * Process and enhance jobs with URL scraping based on platform conditions
     */
    private List<Map<String, Object>> processAndEnhanceJobs(List<Map<String, Object>> jobs) {
        List<Map<String, Object>> enhancedJobs = new ArrayList<>();
        
        for (Map<String, Object> job : jobs) {
            try {
                String platform = (String) job.get("platform");
                String description = (String) job.get("description");
                String applyUrl = (String) job.get("apply_url");
                
                boolean shouldCrawl = false;
                String crawlReason = "";
                
                // Determine if crawling is needed based on platform
                if (platform != null) {
                    String platformLower = platform.toLowerCase();
                    
                    if (platformLower.contains("naukri")) {
                        // Condition 1: Naukri - no crawling
                        shouldCrawl = false;
                        crawlReason = "Naukri platform - crawling disabled";
                    } else if (platformLower.contains("linkedin")) {
                        // Condition 2: LinkedIn - crawl only when description is missing
                        if (description == null || description.trim().isEmpty()) {
                            shouldCrawl = true;
                            crawlReason = "LinkedIn platform with missing description";
                        } else {
                            shouldCrawl = false;
                            crawlReason = "LinkedIn platform with existing description";
                        }
                    } else {
                        // Condition 3: Any other platform - crawl always
                        shouldCrawl = true;
                        crawlReason = "Other platform - always crawl";
                    }
                } else {
                    // Default: crawl if description is missing
                    if (description == null || description.trim().isEmpty()) {
                        shouldCrawl = true;
                        crawlReason = "Unknown platform with missing description";
                    }
                }
                
                if (shouldCrawl && applyUrl != null) {
                    log.info("Crawling URL for job '{}' - Reason: {}", job.get("title"), crawlReason);
                    
                    // Add sleep before crawling to avoid rate limiting
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted during delay before crawling", e);
                    }
                    
                    // Try to enhance with URL scraping
                    Map<String, Object> enhancedJob = enhanceJobWithUrlScraping(job, applyUrl);
                    enhancedJobs.add(enhancedJob);
                } else {
                    log.info("Skipping crawling for job '{}' - Reason: {}", job.get("title"), crawlReason);
                    enhancedJobs.add(job);
                }
                
            } catch (Exception e) {
                log.error("Error processing job: {}", job, e);
                enhancedJobs.add(job); // Add original job if enhancement fails
            }
        }
        
        return enhancedJobs;
    }

    /**
     * Enhance job with URL scraping
     */
    private Map<String, Object> enhanceJobWithUrlScraping(Map<String, Object> job, String applyUrl) {
        try {
            log.info("Enhancing job with URL scraping: {}", applyUrl);
            
            // Extract text from URL using Selenium
            Optional<String> extractedText = webScrapingService.extractTextFromUrl(applyUrl);
            
            if (extractedText.isPresent()) {
                // Save scraping response for analysis
                saveScrapingResponseForAnalysis(job, applyUrl, extractedText.get());
                
                // Use Gemini to extract job details from the scraped text
                String enhancedDescription = extractJobDetailsFromText(extractedText.get(), job);
                
                // Update job with enhanced information
                Map<String, Object> enhancedJob = new HashMap<>(job);
                enhancedJob.put("description", enhancedDescription);
                enhancedJob.put("scraped_content", extractedText.get());
                enhancedJob.put("scraped_at", java.time.LocalDateTime.now().toString());
                enhancedJob.put("scraping_success", true);
                
                return enhancedJob;
            } else {
                // Log failed scraping attempt
                saveScrapingResponseForAnalysis(job, applyUrl, null);
                
                Map<String, Object> enhancedJob = new HashMap<>(job);
                enhancedJob.put("scraping_success", false);
                enhancedJob.put("scraping_error", "Failed to extract text from URL");
                
                return enhancedJob;
            }
            
        } catch (Exception e) {
            log.error("Error enhancing job with URL scraping: {}", applyUrl, e);
            
            // Save error information for analysis
            saveScrapingResponseForAnalysis(job, applyUrl, null);
            
            Map<String, Object> enhancedJob = new HashMap<>(job);
            enhancedJob.put("scraping_success", false);
            enhancedJob.put("scraping_error", e.getMessage());
            
            return enhancedJob;
        }
    }

    /**
     * Save scraping response for analysis
     */
    private void saveScrapingResponseForAnalysis(Map<String, Object> job, String applyUrl, String scrapedText) {
        try {
            // Create a new entity to store scraping analysis data
            UrlContent urlContent = urlContentRepository.findByUrl(applyUrl)
                .orElse(new UrlContent());
            
            urlContent.setUrl(applyUrl);
            urlContent.setExtractedText(scrapedText);
            urlContent.setExtractionStatus(scrapedText != null ? "SUCCESS" : "FAILED");
            urlContent.setErrorMessage(scrapedText == null ? "Failed to extract text" : null);
            urlContent.setExtractedAt(java.time.LocalDateTime.now());
            
            // Add job metadata for analysis
            if (scrapedText != null) {
                String jobMetadata = String.format(
                    "Job Title: %s\nPlatform: %s\nScraped At: %s\nText Length: %d characters",
                    job.get("title"),
                    job.get("platform"),
                    urlContent.getExtractedAt(),
                    scrapedText.length()
                );
                
                // You could add a new field to UrlContent for this metadata
                // For now, we'll log it
                log.info("Scraping Analysis Data: {}", jobMetadata);
            }
            
            urlContentRepository.save(urlContent);
            log.info("Saved scraping response for analysis: {}", applyUrl);
            
        } catch (Exception e) {
            log.error("Error saving scraping response for analysis: {}", applyUrl, e);
        }
    }

    /**
     * Extract job details from scraped text using Gemini
     */
    private String extractJobDetailsFromText(String scrapedText, Map<String, Object> originalJob) {
        try {
            String prompt = String.format(
                "Extract comprehensive job information from the following scraped text. Focus on:\n\n" +
                "1. Detailed job description and responsibilities\n" +
                "2. Required skills, technologies, and qualifications\n" +
                "3. Experience requirements (min/max years)\n" +
                "4. Job type (Remote/Onsite/Hybrid/Work From Office)\n" +
                "5. Salary information (min/max/currency) if available\n" +
                "6. Posting date and application deadline\n" +
                "7. Benefits and perks\n" +
                "8. Company culture and work environment\n" +
                "9. Application process and requirements\n" +
                "10. Any additional requirements or preferences\n\n" +
                "Original job title: %s\n" +
                "Scraped text:\n%s\n\n" +
                "Return a comprehensive job description that includes all relevant details found in the text. " +
                "Structure the response with clear sections for responsibilities, requirements, benefits, etc. " +
                "If the scraped text doesn't contain relevant information, return the original description.",
                originalJob.get("title"),
                scrapedText.substring(0, Math.min(scrapedText.length(), 8000)) // Increased text length limit
            );
            
            Map<String, Object> requestBody = buildRequestBody(prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            
            // Log raw Gemini response for scraping analysis
            log.info("Gemini Scraping Raw Response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Extract text from response
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error extracting job details from text", e);
        }
        
        return (String) originalJob.get("description");
    }

    /**
     * Build request body for Gemini API
     */
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Object[]{part});
        contents.put("contents", new Object[]{content});
        return contents;
    }

    /**
     * Get URL content from database
     */
    public Optional<UrlContent> getUrlContent(String url) {
        return urlContentRepository.findByUrl(url);
    }

    /**
     * Get all URL contents
     */
    public List<UrlContent> getAllUrlContents() {
        return urlContentRepository.findAll();
    }
} 