package com.tymbl.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyShortnameTransactionService {

  @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
  private String apiKey;

  private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Qualifier("aiServiceRestTemplate")
  private final RestTemplate restTemplate;

  private final CompanyRepository companyRepository;

  /**
   * Process company shortname generation and deduplication in a single transaction This method is
   * called via autowiring to ensure proper transaction handling
   */
  @Transactional
  public Map<String, Object> processCompanyShortnameGenerationAndDeduplicationInTransaction(
      Company company) {
    Map<String, Object> result = new HashMap<>();

    try {
      log.info("Processing shortname generation and deduplication for company: {} (ID: {})",
          company.getName(), company.getId());

      // Check if already processed
      if (company.isShortnameGenerated()) {
        result.put("alreadyProcessed", true);
        result.put("success", true);
        result.put("message", "Shortname already generated");
        result.put("shortname", company.getShortname());
        return result;
      }

      // Generate shortname using AI
      String shortname = generateShortnameForCompany(company.getName());

      if (shortname == null || shortname.trim().isEmpty()) {
        result.put("success", false);
        result.put("error", "Failed to generate shortname");
        return result;
      }

      // Check if a company with this shortname already exists
      List<Company> existingCompanies = companyRepository.findByShortnameIgnoreCase(shortname);

      if (!existingCompanies.isEmpty()) {
        // Find the company with the shortest name (likely the main one)
        Company mainCompany = existingCompanies.stream()
            .min((c1, c2) -> Integer.compare(c1.getName().length(), c2.getName().length()))
            .orElse(existingCompanies.get(0));

        if (mainCompany.getId().equals(company.getId())) {
          // This company is already the main one, just update its shortname
          company.setShortname(shortname);
          company.setShortnameGenerated(true);
          companyRepository.save(company);

          result.put("success", true);
          result.put("companyId", company.getId());
          result.put("companyName", company.getName());
          result.put("shortname", shortname);
          result.put("action", "updated");
          result.put("message", "Shortname updated for main company");

          log.info("Updated shortname '{}' for main company: {} (ID: {})", shortname,
              company.getName(), company.getId());
        } else {
          // This company should be deleted as it's a duplicate
          companyRepository.delete(company);

          result.put("success", true);
          result.put("companyId", company.getId());
          result.put("companyName", company.getName());
          result.put("shortname", shortname);
          result.put("action", "deleted");
          result.put("message", "Company deleted as duplicate of: " + mainCompany.getName());
          result.put("mainCompanyId", mainCompany.getId());
          result.put("mainCompanyName", mainCompany.getName());

          log.info("Deleted duplicate company: {} (ID: {}) - main company: {} (ID: {})",
              company.getName(), company.getId(), mainCompany.getName(), mainCompany.getId());
        }
      } else {
        // No existing company with this shortname, save it
        company.setShortname(shortname);
        company.setShortnameGenerated(true);
        companyRepository.save(company);

        result.put("success", true);
        result.put("companyId", company.getId());
        result.put("companyName", company.getName());
        result.put("shortname", shortname);
        result.put("action", "created");
        result.put("message", "Shortname created successfully");

        log.info("Created shortname '{}' for company: {} (ID: {})", shortname, company.getName(),
            company.getId());
      }

    } catch (Exception e) {
      log.error("Error processing shortname generation and deduplication for company: {} (ID: {})",
          company.getName(), company.getId(), e);
      result.put("success", false);
      result.put("error", "Error processing company: " + e.getMessage());
    }

    return result;
  }

  /**
   * Generate shortname for a company using AI This method is self-contained to avoid circular
   * dependency
   */
  private String generateShortnameForCompany(String companyName) {
    try {
      log.info("[TransactionService] Generating shortname for company: {}", companyName);
      String prompt = buildShortnameGenerationPrompt(companyName);
      log.info("[TransactionService] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
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
      log.info("[TransactionService] API response status: {}", response.getStatusCode().value());
      log.info("[TransactionService] API response body length: {}",
          response.getBody() != null ? response.getBody().length() : 0);

      if (response.getStatusCode().value() == 200) {
        String shortname = parseShortnameResponse(response.getBody());
        log.info("[TransactionService] Parsed shortname '{}' for company: {}", shortname,
            companyName);
        return shortname;
      } else {
        log.error("[TransactionService] API error: {} - {}", response.getStatusCode().value(),
            response.getBody());
        return null;
      }
    } catch (Exception e) {
      log.error("[TransactionService] Error generating shortname for company: {}", companyName, e);
      return null;
    }
  }

  /**
   * Build prompt for shortname generation
   */
  private String buildShortnameGenerationPrompt(String companyName) {
    String prompt = String.format(
        "You are a business and technology expert helping to identify the most commonly used and recognized shortnames for companies. " +
        "Given the company name, provide the most widely recognized and commonly used shortname or nickname for this company.\n\n" +
        "CRITICAL REQUIREMENTS:\n" +
        "1. Focus on the CORE company name that people actually use in conversation\n" +
        "2. Remove ALL legal suffixes, descriptive text, and extra information\n" +
        "3. Return the shortest, most recognizable version of the company name\n" +
        "4. Consider how people actually refer to the company in daily conversation\n" +
        "5. If no widely recognized shortname exists, return the cleaned core name\n\n" +
        "COMPREHENSIVE PATTERNS TO HANDLE:\n" +
        "LEGAL SUFFIXES: Remove 'Limited', 'Ltd', 'Ltd.', 'Private Limited', 'Pvt Ltd', 'Incorporated', 'Inc', 'Inc.', 'Corporation', 'Corp', 'Corp.', 'Company', 'Co', 'Co.'\n" +
        "BUSINESS TYPES: Remove 'Group', 'Grp', 'Holdings', 'Holding', 'Enterprises', 'Enterprise', 'Industries', 'Industry', 'Solutions', 'Solution'\n" +
        "TECHNOLOGY TERMS: Remove 'Technologies', 'Technology', 'Tech', 'Tech.', 'Systems', 'System', 'Services', 'Service'\n" +
        "GEOGRAPHIC TERMS: Remove 'International', 'Intl', 'Intl.', 'Global', 'Worldwide', 'World Wide'\n" +
        "INTERNATIONAL FORMATS: Remove 'PLC', 'LLC', 'AG', 'GmbH', 'SA', 'NV', 'BV', 'AB', 'OY', 'AS', 'KK'\n" +
        "ASIAN FORMATS: Remove 'Kabushiki Kaisha', 'K.K.', 'Yugen Kaisha', 'Y.K.', 'Godo Kaisha', 'G.K.'\n" +
        "EUROPEAN FORMATS: Remove 'Societe Anonyme', 'S.A.', 'Societa per Azioni', 'S.p.A.', 'Sociedad Anonima', 'Naamloze Vennootschap', 'N.V.', 'Besloten Vennootschap', 'B.V.'\n" +
        "PUNCTUATION: Remove trailing periods, commas, semicolons, and other punctuation\n" +
        "EXTRA TEXT: Remove any additional descriptive text, taglines, or marketing phrases\n\n" +
        "EXAMPLES:\n" +
        "- 'Zydus Lifesciences Limited' → 'Zydus Lifesciences'\n" +
        "- 'Microsoft Corporation' → 'Microsoft'\n" +
        "- 'Apple Inc.' → 'Apple'\n" +
        "- 'Google LLC' → 'Google'\n" +
        "- 'Amazon.com, Inc.' → 'Amazon'\n" +
        "- 'Tesla, Inc.' → 'Tesla'\n" +
        "- 'Netflix, Inc.' → 'Netflix'\n" +
        "- 'Meta Platforms, Inc.' → 'Meta'\n" +
        "- 'Alphabet Inc.' → 'Alphabet'\n" +
        "- 'NVIDIA Corporation' → 'NVIDIA'\n\n" +
        "RESPONSE FORMAT:\n" +
        "Return ONLY the shortname, nothing else. No explanations, no quotes, no additional text.\n\n" +
        "Company name: %s\n" +
        "Shortname:",
        companyName
    );
    return prompt;
  }

  /**
   * Parse the shortname from the GenAI response
   */
  private String parseShortnameResponse(String responseBody) {
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
            // Clean up the response - remove any extra text and extract just the shortname
            String cleanedText = text.trim();

            // Remove quotes if present
            if (cleanedText.startsWith("\"") && cleanedText.endsWith("\"")) {
              cleanedText = cleanedText.substring(1, cleanedText.length() - 1);
            }

            // Remove any trailing punctuation
            cleanedText = cleanedText.replaceAll("[.,;:]$", "").trim();

            return cleanedText;
          }
        }
      }

      log.warn("Could not parse shortname from response: {}", responseBody);
      return null;
    } catch (Exception e) {
      log.error("Error parsing shortname response: {}", responseBody, e);
      return null;
    }
  }

  /**
   * Build request body for GenAI API
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
} 