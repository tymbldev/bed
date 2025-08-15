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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AIJobFetchingService {

  private final WebScrapingService webScrapingService;
  private final UrlContentRepository urlContentRepository;
  private final ObjectMapper objectMapper;
  private final AIRestService aiRestService;

  public AIJobFetchingService(
      WebScrapingService webScrapingService,
      UrlContentRepository urlContentRepository,
      ObjectMapper objectMapper,
      AIRestService aiRestService) {
    this.webScrapingService = webScrapingService;
    this.urlContentRepository = urlContentRepository;
    this.objectMapper = objectMapper;
    this.aiRestService = aiRestService;
  }


  /**
   * Fetch jobs for a company using the updated two-step workflow
   *
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
        "You are an expert job researcher with access to real-time job market data. Your task is to find authentic, current job listings for '%s'.\n\n"
            +
            "INSTRUCTIONS:\n" +
            "1. Take as much time as you need to research thoroughly\n" +
            "2. Search multiple reliable sources including:\n" +
            "   - Company's official careers page\n" +
            "   - LinkedIn Jobs\n" +
            "   - Naukri.com\n" +
            "   - Indeed\n" +
            "   - Glassdoor\n" +
            "   - Greenhouse, Lever, or other ATS platforms\n" +
            "   - Any other legitimate job portals\n\n" +
            "3. Verify each job listing is currently active and publicly available\n" +
            "4. Extract accurate, detailed information for each job\n" +
            "5. Do NOT guess, fabricate, or include expired/inactive jobs\n" +
            "6. If you cannot find any current jobs, return an empty array []\n" +
            "7. Quality - Make sure all jobs are authentic\n" +
            "RESEARCH PROCESS:\n" +
            "- Search the company name + \"careers\" or \"jobs\"\n" +
            "- Check multiple job platforms for the same company\n" +
            "- Verify job postings are recent (within last 30 days)\n" +
            "- Cross-reference information across sources when possible\n\n" +
            "Return ONLY a valid JSON array with the exact format below. No explanations, comments, or additional text:\n\n"
            +
            "[\n" +
            "  {\n" +
            "    \"title\": \"Exact job title as posted\",\n" +
            "    \"designation\": \"Job designation/role name\",\n" +
            "    \"description\": \"Brief but informative job description\",\n" +
            "    \"location\": {\n" +
            "      \"city\": \"City name\",\n" +
            "      \"country\": \"Country name\"\n" +
            "    },\n" +
            "    \"job_type\": \"Remote Only|Onsite|Hybrid|Work From Office\",\n" +
            "    \"salary\": {\n" +
            "      \"min\": 50000,\n" +
            "      \"max\": 80000,\n" +
            "      \"currency\": \"USD|INR|EUR\"\n" +
            "    },\n" +
            "    \"experience\": {\n" +
            "      \"min\": 2,\n" +
            "      \"max\": 5\n" +
            "    },\n" +
            "    \"skills\": [\"Java\", \"Spring Boot\", \"React\"],\n" +
            "    \"tags\": [\"Full-time\", \"Senior Level\", \"Tech\"],\n" +
            "    \"openings\": 3,\n" +
            "    \"posted\": \"2025-01-15\",\n" +
            "    \"platform\": \"LinkedIn|Naukri|Company Website|Indeed\",\n" +
            "    \"apply_url\": \"https://example.com/apply/job123\"\n" +
            "  }\n" +
            "]\n\n" +
            "CRITICAL REQUIREMENTS:\n" +
            "- Return ONLY the JSON array, nothing else\n" +
            "- Ensure all URLs are valid and accessible\n" +
            "- Include only currently active job postings\n" +
            "- If no jobs found, return: []\n" +
            "- Take your time to provide accurate, verified information",
        companyName
    );
  }

  /**
   * Build the detailed job fetching prompt (Step 2)
   */
  private String buildDetailedJobPrompt(String jobTitle, String applyUrl) {
    return String.format(
        "You are an expert job analyst. Your task is to extract comprehensive, detailed information for the job '%s' from the URL '%s'.\n\n"
            +
            "INSTRUCTIONS:\n" +
            "1. Take as much time as you need to thoroughly analyze the job posting\n" +
            "2. Extract ALL available information from the provided URL\n" +
            "3. Be comprehensive and detailed in your analysis\n" +
            "4. If the URL is not accessible, use your knowledge to provide realistic job details\n"
            +
            "5. Ensure all information is accurate and relevant to the job title\n" +
            "6. Quality over quantity - provide detailed, useful information\n\n" +
            "ANALYSIS PROCESS:\n" +
            "- Read and understand the complete job description\n" +
            "- Extract all responsibilities, requirements, and qualifications\n" +
            "- Identify technical skills, technologies, and tools mentioned\n" +
            "- Note any benefits, perks, or company culture information\n" +
            "- Understand the application process and requirements\n" +
            "- Look for any additional preferences or nice-to-have skills\n\n" +
            "Return ONLY a valid JSON object with the exact format below. No explanations, comments, or additional text:\n\n"
            +
            "{\n" +
            "  \"detailed_description\": \"Comprehensive job description with all key details\",\n"
            +
            "  \"responsibilities\": [\"Key responsibility 1\", \"Key responsibility 2\", \"Key responsibility 3\"],\n"
            +
            "  \"requirements\": [\"Required qualification 1\", \"Required qualification 2\", \"Required qualification 3\"],\n"
            +
            "  \"benefits\": [\"Benefit 1\", \"Benefit 2\", \"Benefit 3\"],\n" +
            "  \"application_process\": \"Detailed application process and requirements\",\n" +
            "  \"additional_info\": \"Any additional information, preferences, or notes\"\n" +
            "}\n\n" +
            "CRITICAL REQUIREMENTS:\n" +
            "- Return ONLY the JSON object, nothing else\n" +
            "- Be thorough and comprehensive in your analysis\n" +
            "- Include all relevant information found in the job posting\n" +
            "- Use realistic and accurate information\n" +
            "- Take your time to provide detailed, quality responses\n" +
            "- If information is not available, use reasonable defaults based on the job title",
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
      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody, "Job Fetching");

      // Log raw Gemini response
      log.info("Gemini API Raw Response - Status: {}, Body: {}", response.getStatusCode(),
          response.getBody());

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
      List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get(
          "candidates");

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
        return objectMapper.readValue(jsonText, new TypeReference<List<Map<String, Object>>>() {
        });
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
  private void saveScrapingResponseForAnalysis(Map<String, Object> job, String applyUrl,
      String scrapedText) {
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
              "Return a comprehensive job description that includes all relevant details found in the text. "
              +
              "Structure the response with clear sections for responsibilities, requirements, benefits, etc. "
              +
              "If the scraped text doesn't contain relevant information, return the original description.",
          originalJob.get("title"),
          scrapedText.substring(0, Math.min(scrapedText.length(), 8000))
          // Increased text length limit
      );

      Map<String, Object> requestBody = buildRequestBody(prompt);
      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Job Details Extraction");

      // Log raw Gemini response for scraping analysis
      log.info("Gemini Scraping Raw Response - Status: {}, Body: {}", response.getStatusCode(),
          response.getBody());

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        // Extract text from response
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get(
            "candidates");

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