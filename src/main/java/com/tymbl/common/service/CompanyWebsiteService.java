package com.tymbl.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyWebsiteService {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AIRestService aiRestService;

  private final CompanyRepository companyRepository;

  /**
   * Fetch website URL for a single company using GenAI
   */
  public Map<String, Object> fetchWebsiteForCompany(Company company) {
    Map<String, Object> result = new HashMap<>();

    try {
      log.info("Fetching website for company: {} (ID: {})", company.getName(), company.getId());

      // Check if already processed
      if (company.getWebsiteFetched() != null && company.getWebsiteFetched() == 1) {
        result.put("alreadyProcessed", true);
        result.put("success", true);
        result.put("message", "Website already fetched");
        result.put("website", company.getWebsite());
        return result;
      }

      // Generate website URL using AI
      String website = generateWebsiteForCompany(company.getName());

      if (website == null || website.trim().isEmpty()) {
        company.setWebsiteFetched(2); // Mark as failed
        companyRepository.save(company);

        result.put("success", false);
        result.put("error", "Failed to generate website URL");
        return result;
      }

      // Save the website URL
      company.setWebsite(website);
      company.setWebsiteFetched(1); // Mark as fetched
      companyRepository.save(company);

      result.put("success", true);
      result.put("companyId", company.getId());
      result.put("companyName", company.getName());
      result.put("website", website);
      result.put("message", "Website URL fetched successfully");

      log.info("Fetched website URL '{}' for company: {} (ID: {})", website, company.getName(),
          company.getId());

    } catch (Exception e) {
      log.error("Error fetching website for company: {} (ID: {})", company.getName(),
          company.getId(), e);
      company.setWebsiteFetched(2); // Mark as failed
      companyRepository.save(company);

      result.put("success", false);
      result.put("error", "Error fetching website: " + e.getMessage());
    }

    return result;
  }

  /**
   * Fetch websites for all companies in batches using database pagination
   */
  public Map<String, Object> fetchWebsitesForAllCompaniesInBatches() {
    log.info("Starting website fetching for all companies using database pagination");

    List<Map<String, Object>> companyResults = new ArrayList<>();
    int totalProcessed = 0;
    int totalWebsitesFetched = 0;
    int totalErrors = 0;
    int batchSize = 10; // Process 10 companies at a time
    int pageNumber = 0;

    while (true) {
      // Fetch companies that haven't been processed for website fetching (status = 0 or null)
      Page<Company> companyPage = companyRepository.findByWebsiteFetched(0,
          PageRequest.of(pageNumber, batchSize));

      List<Company> companies = companyPage.getContent();

      if (companies.isEmpty()) {
        log.info("No more companies to process for website fetching. Completed at page: {}",
            pageNumber);
        break;
      }

      log.info("Processing website fetching batch {} with {} companies", pageNumber + 1,
          companies.size());

      for (Company company : companies) {
        try {
          Map<String, Object> result = fetchWebsiteForCompany(company);
          companyResults.add(result);

          if ((Boolean) result.get("success")) {
            totalWebsitesFetched++;
            log.info("Successfully fetched website for company: {} (ID: {})", company.getName(),
                company.getId());
          } else {
            totalErrors++;
            log.error("Failed to fetch website for company: {} (ID: {})", company.getName(),
                company.getId());
          }

          totalProcessed++;

        } catch (Exception e) {
          totalErrors++;
          log.error("Error fetching website for company: {} (ID: {})", company.getName(),
              company.getId(), e);

          Map<String, Object> errorResult = new HashMap<>();
          errorResult.put("companyId", company.getId());
          errorResult.put("companyName", company.getName());
          errorResult.put("success", false);
          errorResult.put("error", "Error fetching website: " + e.getMessage());
          companyResults.add(errorResult);
        }
      }

      pageNumber++;
    }

    Map<String, Object> result = new HashMap<>();
    result.put("totalProcessed", totalProcessed);
    result.put("totalWebsitesFetched", totalWebsitesFetched);
    result.put("totalErrors", totalErrors);
    result.put("companyResults", companyResults);
    result.put("message", "Website fetching completed using database pagination");

    log.info(
        "Completed website fetching using database pagination. Total processed: {}, Websites fetched: {}, Errors: {}",
        totalProcessed, totalWebsitesFetched, totalErrors);

    return result;
  }

  /**
   * Generate website URL for a company using GenAI
   */
  private String generateWebsiteForCompany(String companyName) {
    try {
      log.info("[Gemini] Generating website URL for company: {}", companyName);
      String prompt = buildWebsiteGenerationPrompt(companyName);
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Website Generation for " + companyName);
      String website = parseWebsiteResponse(response.getBody());
      log.info("[Gemini] Parsed website URL '{}' for company: {}", website, companyName);
      return website;
    } catch (Exception e) {
      log.error("[Gemini] Error generating website URL for company: {}", companyName, e);
      return null;
    }
  }

  /**
   * Build prompt for website URL generation
   */
  private String buildWebsiteGenerationPrompt(String companyName) {
    return String.format(
        "You are a business and technology expert helping to find the official website URL for companies. "
            +
            "Given the company name, provide the official website URL for this company.\n\n" +
            "CRITICAL REQUIREMENTS:\n" +
            "1. Return ONLY the official website URL for the company\n" +
            "2. The URL should be the main corporate website (not social media, job boards, etc.)\n"
            +
            "3. Include the full URL with https:// protocol\n" +
            "4. Ensure the URL is currently active and accessible\n" +
            "5. Prefer the most commonly known and used website URL\n\n" +
            "PREFERRED FORMATS:\n" +
            "- https://www.companyname.com\n" +
            "- https://companyname.com\n" +
            "- https://www.companyname.org (for non-profits)\n" +
            "- https://companyname.co (for startups/tech companies)\n\n" +
            "EXAMPLES OF GOOD WEBSITE URLS:\n" +
            "- https://www.apple.com\n" +
            "- https://www.google.com\n" +
            "- https://www.microsoft.com\n" +
            "- https://www.amazon.com\n" +
            "- https://www.netflix.com\n" +
            "- https://www.tesla.com\n" +
            "- https://www.meta.com\n" +
            "- https://www.linkedin.com\n\n" +
            "AVOID:\n" +
            "- Social media URLs (facebook.com, twitter.com, etc.)\n" +
            "- Job board URLs (indeed.com, glassdoor.com, etc.)\n" +
            "- News article URLs\n" +
            "- Wikipedia URLs\n" +
            "- URLs with specific page paths (e.g., /about, /careers)\n\n" +
            "RESPONSE FORMAT:\n" +
            "Return ONLY the website URL, nothing else. No explanations, no quotes, no additional text.\n\n"
            +
            "Company name: %s\n" +
            "Website URL:",
        companyName
    );
  }

  /**
   * Parse the website URL from the GenAI response
   */
  private String parseWebsiteResponse(String responseBody) {
    try {
      Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
      List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get(
          "candidates");

      if (candidates != null && !candidates.isEmpty()) {
        Map<String, Object> candidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        if (parts != null && !parts.isEmpty()) {
          Map<String, Object> part = parts.get(0);
          String text = (String) part.get("text");

          if (text != null && !text.trim().isEmpty()) {
            // Clean up the response - remove any extra text and extract just the URL
            String cleanedText = text.trim();

            // If the response contains a URL, extract it
            if (cleanedText.startsWith("http")) {
              // Find the first URL in the text
              String[] lines = cleanedText.split("\n");
              for (String line : lines) {
                line = line.trim();
                if (line.startsWith("http")) {
                  return line;
                }
              }
            }

            return cleanedText;
          }
        }
      }

      log.warn("Could not parse website URL from response: {}", responseBody);
      return null;
    } catch (Exception e) {
      log.error("Error parsing website URL response: {}", responseBody, e);
      return null;
    }
  }


} 