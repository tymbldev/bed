package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.dto.CompanyGenerationResponse;
import com.tymbl.common.entity.Industry;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.jobs.entity.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiCompanyService {

  @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
  private String apiKey;

  private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Qualifier("aiServiceRestTemplate")
  private final RestTemplate restTemplate;
  private final IndustryRepository industryRepository;
  private final CompanyPersistenceService companyPersistenceService;

  public CompanyGenerationResponse generateCompanyInfo(String companyName) {
    try {
      log.info("Starting company information generation for: {} using Gemini AI", companyName);
      if (companyName == null || companyName.trim().isEmpty()) {
        log.warn("Company name is null or empty");
        return CompanyGenerationResponse.builder()
            .success(false)
            .errorMessage("Company name is null or empty")
            .build();
      }
      String prompt = buildCompanyGenerationPrompt(companyName);
      Map<String, Object> requestBody = buildRequestBody(prompt);
      log.debug("Sending request to Gemini API for company: {}", companyName);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      try {
        ResponseEntity<String> response = restTemplate.exchange(
            GEMINI_API_URL + "?key=" + apiKey,
            HttpMethod.POST,
            request,
            String.class
        );
        log.debug("Gemini API response status: {}", response.getStatusCodeValue());
        if (response.getStatusCodeValue() == 200) {
          log.info("Successfully received response from Gemini API for company: {}", companyName);
          return parseGeminiResponseWithJunkDetection(response.getBody(), companyName);
        } else {
          log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
          return CompanyGenerationResponse.builder()
              .success(false)
              .errorMessage("Gemini API error: " + response.getStatusCodeValue())
              .build();
        }
      } catch (HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
          log.error("Rate limit exceeded for Gemini API. Company: {}", companyName);
          log.error("Rate limit error details: {}", e.getResponseBodyAsString());
          throw new RuntimeException("Rate limit exceeded for Gemini API");
        } else {
          log.error("HTTP error calling Gemini API for company: {}", companyName, e);
          return CompanyGenerationResponse.builder()
              .success(false)
              .errorMessage("HTTP error: " + e.getMessage())
              .build();
        }
      } catch (ResourceAccessException e) {
        if (e.getCause() instanceof java.net.SocketTimeoutException) {
          log.error("Request timeout for Gemini API. Company: {}", companyName, e);
          throw new RuntimeException("Request timeout for Gemini API", e);
        } else {
          log.error("Connection error calling Gemini API for company: {}", companyName, e);
          return CompanyGenerationResponse.builder()
              .success(false)
              .errorMessage("Connection error: " + e.getMessage())
              .build();
        }
      } catch (RuntimeException e) {
        if (e.getMessage() != null && e.getMessage().contains("timeout")) {
          log.error("Request timeout for Gemini API. Company: {}", companyName, e);
          throw e;
        } else {
          log.error("Runtime error calling Gemini API for company: {}", companyName, e);
          return CompanyGenerationResponse.builder()
              .success(false)
              .errorMessage("Runtime error: " + e.getMessage())
              .build();
        }
      }
    } catch (Exception e) {
      log.error("Error calling Gemini API for company: {}", companyName, e);
      return CompanyGenerationResponse.builder()
          .success(false)
          .errorMessage("Unexpected error: " + e.getMessage())
          .build();
    }
  }

  public Map<String, Object> detectCompanyIndustries(String companyName, String companyDescription,
      String specialties) {
    try {
      log.info("Starting company industry detection for: {} using Gemini AI", companyName);
      if (companyName == null || companyName.trim().isEmpty()) {
        log.warn("Company name is null or empty");
        return new HashMap<>();
      }
      String prompt = buildIndustryDetectionPrompt(companyName, companyDescription, specialties);
      Map<String, Object> requestBody = buildRequestBody(prompt);
      log.debug("Sending request to Gemini API for industry detection: {}", companyName);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      try {
        ResponseEntity<String> response = restTemplate.exchange(
            GEMINI_API_URL + "?key=" + apiKey,
            HttpMethod.POST,
            request,
            String.class
        );
        log.debug("Gemini API response status: {}", response.getStatusCodeValue());
        if (response.getStatusCodeValue() == 200) {
          log.info("Successfully received response from Gemini API for industry detection: {}",
              companyName);
          return parseIndustryResponse(response.getBody());
        } else {
          log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
          return new HashMap<>();
        }
      } catch (HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
          log.error("Rate limit exceeded for Gemini API. Company: {}", companyName);
          log.error("Rate limit error details: {}", e.getResponseBodyAsString());
          throw new RuntimeException("Rate limit exceeded for Gemini API");
        } else {
          log.error("HTTP error calling Gemini API for industry detection: {}", companyName, e);
          return new HashMap<>();
        }
      } catch (ResourceAccessException e) {
        if (e.getCause() instanceof java.net.SocketTimeoutException) {
          log.error("Request timeout for Gemini API. Company: {}", companyName, e);
          throw new RuntimeException("Request timeout for Gemini API", e);
        } else {
          log.error("Connection error calling Gemini API for industry detection: {}", companyName,
              e);
          return new HashMap<>();
        }
      } catch (RuntimeException e) {
        if (e.getMessage() != null && e.getMessage().contains("timeout")) {
          log.error("Request timeout for Gemini API. Company: {}", companyName, e);
          throw e;
        } else {
          log.error("Runtime error calling Gemini API for industry detection: {}", companyName, e);
          return new HashMap<>();
        }
      }
    } catch (Exception e) {
      log.error("Error calling Gemini API for industry detection: {}", companyName, e);
      return new HashMap<>();
    }
  }

  /**
   * Generate a list of companies (name, website) for a given industry, excluding provided names.
   *
   * @param industryName The industry to generate companies for
   * @param excludeNames List of company names to ignore (case-insensitive)
   * @return List of maps with keys 'name' and 'website'
   */
  public List<Map<String, String>> generateCompanyListForIndustry(String industryName,
      List<String> excludeNames) {
    try {
      StringBuilder prompt = new StringBuilder();
      prompt.append("Give a random list of 500 real INDIAN companies in the '")
          .append(industryName)
          .append(
              "' industry. Only include companies that are based in India or are Indian in origin. For each, provide company name and website. ");
      if (excludeNames != null && !excludeNames.isEmpty()) {
        prompt.append("Ignore these companies (do not include them in your response): ");
        prompt.append(String.join(", ", excludeNames));
        prompt.append(". ");
      }
      prompt.append(
          "Your response MUST strictly match this schema: [{\"name\": string, \"website\": string}, ...] (500 items). ");
      prompt.append("Return ONLY a JSON array of 500 objects with 'name' and 'website' fields. ");
      prompt.append(
          "Do NOT include any explanation, disclaimer, markdown, or text before or after the array—just the JSON array. ");
      prompt.append("Example: [{\"name\":\"Infosys\",\"website\":\"https://www.infosys.com\"}]");
      prompt.append(
          "IMPORTANT: My system will parse your response as JSON. If you add any note, remark, explanation, or text before or after the array, the process will fail. Do NOT add anything except the JSON array.");

      Map<String, Object> requestBody = buildRequestBody(prompt.toString());
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      ResponseEntity<String> response = restTemplate.exchange(
          GEMINI_API_URL + "?key=" + apiKey,
          HttpMethod.POST,
          request,
          String.class
      );
      Thread.sleep(1000); // Add a delay to avoid hitting rate limits too quickly
      if (response.getStatusCodeValue() == 200) {
        return parseCompanyListResponse(response.getBody());
      } else {
        log.error("Gemini API error (company list): {} - {}", response.getStatusCodeValue(),
            response.getBody());
        return Collections.emptyList();
      }
    } catch (Exception e) {
      log.error("Error generating company list for industry {}: {}", industryName, e.getMessage(),
          e);
      return Collections.emptyList();
    }
  }

  private String buildCompanyGenerationPrompt(String companyName) {
    return "Analyze the company name '" + companyName
        + "' and determine if it's a valid, real company name.\n\n" +
        "JUNK COMPANY NAME DETECTION:\n" +
        "First, evaluate if this is a valid company name. Consider these criteria:\n" +
        "1. Is it a real, existing company? (not a product, service, technology, or generic term)\n"
        +
        "2. Is it a complete company name? (not just a brand, product line, or technology)\n" +
        "3. Is it specific enough to identify a unique company? (not too generic or ambiguous)\n" +
        "4. Is it a legitimate business entity? (not a fictional company, joke, or placeholder)\n\n"
        +
        "EXAMPLES OF JUNK/INVALID NAMES:\n" +
        "- 'Azure' (Microsoft product, not a company)\n" +
        "- 'React' (Facebook library, not a company)\n" +
        "- 'Java' (Oracle technology, not a company)\n" +
        "- 'Cloud' (generic term, not a company)\n" +
        "- 'AI' (generic term, not a company)\n" +
        "- 'Startup' (generic term, not a company)\n" +
        "- 'Tech' (generic term, not a company)\n" +
        "- 'Solutions' (generic term, not a company)\n" +
        "- 'Services' (generic term, not a company)\n" +
        "- 'Corp' (generic term, not a company)\n" +
        "- 'Inc' (generic term, not a company)\n" +
        "- 'LLC' (generic term, not a company)\n\n" +
        "EXAMPLES OF VALID COMPANY NAMES:\n" +
        "- 'Microsoft Corporation'\n" +
        "- 'Google LLC'\n" +
        "- 'Amazon Web Services'\n" +
        "- 'Salesforce Inc'\n" +
        "- 'Oracle Corporation'\n" +
        "- 'Adobe Inc'\n" +
        "- 'Netflix Inc'\n" +
        "- 'Spotify Technology'\n\n" +
        "If the name is JUNK/INVALID, return ONLY this JSON:\n" +
        "{\n" +
        "  \"junk_identified\": true,\n" +
        "  \"junk_reason\": \"[specific reason why this is not a valid company name]\"\n" +
        "}\n\n" +
        "If the name is VALID, generate detailed company information and return this JSON:\n" +
        "{\n" +
        "  \"junk_identified\": false,\n" +
        "  \"description\": \"[detailed company description]\",\n" +
        "  \"about_us\": \"[detailed about us information]\",\n" +
        "  \"mission\": \"[company mission statement]\",\n" +
        "  \"vision\": \"[company vision statement]\",\n" +
        "  \"culture\": \"[company culture description]\",\n" +
        "  \"specialties\": \"[company specialties and focus areas]\",\n" +
        "  \"company_size\": \"[employee count and company scale]\",\n" +
        "  \"headquarters\": \"[company headquarters location]\",\n" +
        "  \"career_page_url\": \"[valid URL or null]\",\n" +
        "  \"website\": \"[valid URL or null]\",\n" +
        "  \"logo_url\": \"[valid URL or null]\",\n" +
        "  \"linkedin_url\": \"[valid URL or null]\"\n" +
        "}\n\n" +
        "IMPORTANT RULES:\n" +
        "1. For URLs: Must be valid URLs starting with http/https. If not available, return null.\n"
        +
        "2. For all fields: If information is not available, return null (not empty strings or placeholders).\n"
        +
        "3. Be thorough in junk detection - when in doubt, mark as junk.\n" +
        "4. Provide detailed, comprehensive information for valid companies.\n" +
        "5. Do not include explanations or instructions in the response - only the JSON.";
  }

  private String buildIndustryDetectionPrompt(String companyName, String companyDescription,
      String specialties) {
    // Get all available industries from database
    List<Industry> allIndustries = industryRepository.findAll();
    List<String> industryNames = allIndustries.stream()
        .map(Industry::getName)
        .collect(Collectors.toList());

    StringBuilder prompt = new StringBuilder();
    prompt.append("Analyze the following company details and identify its industries and tags:\n");
    prompt.append("Company Name: ").append(companyName).append("\n");
    prompt.append("Company Description: ").append(companyDescription).append("\n");
    prompt.append("Specialties: ").append(specialties).append("\n\n");

    prompt.append("AVAILABLE PRIMARY INDUSTRIES (choose the most appropriate one):\n");
    for (String industry : industryNames) {
      prompt.append("- ").append(industry).append("\n");
    }

    prompt.append(
        "\nSECONDARY INDUSTRIES & TAGS (choose 2-6 items from industries above OR relevant tags):\n");
    prompt.append("Secondary items can include:\n");
    prompt.append("1. Additional industries from the list above\n");
    prompt.append("2. Company type tags such as:\n");
    prompt.append("   - 'Startup' (for early-stage companies)\n");
    prompt.append("   - 'Fortune 500' (for large established companies)\n");
    prompt.append("   - 'Unicorn' (for companies valued over $1B)\n");
    prompt.append("   - 'Product Based Company' (for product-focused companies)\n");
    prompt.append("   - 'Service Based Company' (for service-focused companies)\n");
    prompt.append("   - 'SaaS' (for Software-as-a-Service companies)\n");
    prompt.append("   - 'B2B' (for Business-to-Business companies)\n");
    prompt.append("   - 'B2C' (for Business-to-Consumer companies)\n");
    prompt.append("   - 'Enterprise' (for enterprise-focused companies)\n");
    prompt.append("   - 'SME' (for Small and Medium Enterprises)\n");
    prompt.append("   - 'Public Company' (for publicly traded companies)\n");
    prompt.append("   - 'Private Company' (for privately held companies)\n");
    prompt.append("   - 'Remote First' (for companies with remote-first culture)\n");
    prompt.append("   - 'Hybrid' (for companies with hybrid work model)\n");
    prompt.append("   - 'Onsite' (for companies requiring office presence)\n");

    prompt.append("\nExamples of secondary industry and tag combinations:\n");
    prompt.append(
        "- For a fintech startup: Primary: 'FinTech', Secondary: 'Financial Services, Software Development, Startup, B2B, SaaS'\n");
    prompt.append(
        "- For a healthtech unicorn: Primary: 'Healthcare & HealthTech', Secondary: 'Software Development, AI/ML, Unicorn, Product Based Company, B2B'\n");
    prompt.append(
        "- For a Fortune 500 e-commerce company: Primary: 'E-commerce & Online Retail', Secondary: 'Software Development, MarTech/AdTech, Fortune 500, B2C, Enterprise'\n");
    prompt.append(
        "- For a cybersecurity startup: Primary: 'Cybersecurity', Secondary: 'Software Development, IT Services, Startup, B2B, SaaS'\n");
    prompt.append(
        "- For an AI enterprise company: Primary: 'AI/ML', Secondary: 'Software Development, Data Analytics, Enterprise, B2B, Product Based Company'\n");

    prompt.append("\nINSTRUCTIONS:\n");
    prompt.append(
        "1. For PRIMARY INDUSTRY: Select exactly ONE industry from the available list that best represents the company's main business focus.\n");
    prompt.append("2. For SECONDARY INDUSTRIES & TAGS: Select 2-6 items that can be either:\n");
    prompt.append("   - Additional industries from the provided list\n");
    prompt.append("   - Relevant company type tags (Startup, Fortune 500, Unicorn, etc.)\n");
    prompt.append("   - Business model indicators (B2B, B2C, SaaS, etc.)\n");
    prompt.append(
        "3. Consider the company's description, specialties, size, business model, and work culture when making selections.\n");
    prompt.append(
        "4. Mix industries and tags appropriately based on the company's characteristics.\n\n");

    prompt.append("Provide response in this JSON format:\n");
    prompt.append("{\n");
    prompt.append("  \"primaryIndustry\": \"[selected primary industry name]\",\n");
    prompt.append(
        "  \"secondaryIndustries\": [\"[industry1 or tag1]\", \"[industry2 or tag2]\", \"[industry3 or tag3]\"]\n");
    prompt.append("}");

    return prompt.toString();
  }

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

  private CompanyGenerationResponse parseGeminiResponseWithJunkDetection(String responseBody,
      String companyName) {
    try {
      JsonNode responseNode = objectMapper.readTree(responseBody);
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            log.debug("Raw generated text from Gemini: {}", generatedText);
            String jsonText = extractJsonFromText(generatedText);
            log.debug("Extracted JSON text: {}", jsonText);
            try {
              JsonNode companyData = objectMapper.readTree(jsonText);

              // Check if this is a junk response
              if (companyData.has("junk_identified") && companyData.get("junk_identified")
                  .asBoolean()) {
                String junkReason = companyData.has("junk_reason") ?
                    companyData.get("junk_reason").asText() : "Invalid company name";
                log.info("Company '{}' identified as junk: {}", companyName, junkReason);
                return CompanyGenerationResponse.builder()
                    .success(true)
                    .junkIdentified(true)
                    .junkReason(junkReason)
                    .build();
              }

              // Process as valid company
              Optional<Company> companyOpt = companyPersistenceService.mapJsonToCompany(companyName,
                  companyData);
              if (companyOpt.isPresent()) {
                Company company = companyOpt.get();
                company.setJunkIdentified(false); // Ensure it's marked as not junk
                return CompanyGenerationResponse.builder()
                    .success(true)
                    .junkIdentified(false)
                    .company(company)
                    .build();
              } else {
                return CompanyGenerationResponse.builder()
                    .success(false)
                    .errorMessage("Failed to map company data")
                    .build();
              }
            } catch (Exception jsonParseException) {
              log.error("Failed to parse extracted JSON: {}", jsonText, jsonParseException);
              return CompanyGenerationResponse.builder()
                  .success(false)
                  .errorMessage("Failed to parse JSON: " + jsonParseException.getMessage())
                  .build();
            }
          }
        }
      }
      log.error("Unexpected Gemini API response structure: {}", responseBody);
      return CompanyGenerationResponse.builder()
          .success(false)
          .errorMessage("Unexpected API response structure")
          .build();
    } catch (Exception e) {
      log.error("Error parsing Gemini response", e);
      return CompanyGenerationResponse.builder()
          .success(false)
          .errorMessage("Error parsing response: " + e.getMessage())
          .build();
    }
  }

  private Map<String, Object> parseIndustryResponse(String responseBody) {
    try {
      JsonNode responseNode = objectMapper.readTree(responseBody);
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            log.debug("Raw generated text from Gemini: {}", generatedText);
            String jsonText = extractJsonFromText(generatedText);
            log.debug("Extracted JSON text: {}", jsonText);
            try {
              JsonNode industryData = objectMapper.readTree(jsonText);
              return mapJsonToIndustries(industryData);
            } catch (Exception jsonParseException) {
              log.error("Failed to parse extracted JSON: {}", jsonText, jsonParseException);
              return new HashMap<>();
            }
          }
        }
      }
      log.error("Unexpected Gemini API response structure for industries: {}", responseBody);
      return new HashMap<>();
    } catch (Exception e) {
      log.error("Error parsing Gemini response for industries", e);
      return new HashMap<>();
    }
  }

  private List<Map<String, String>> parseCompanyListResponse(String responseBody) {
    try {
      JsonNode responseNode = objectMapper.readTree(responseBody);
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            String jsonText = extractJsonFromText(generatedText);
            try {
              JsonNode arr = objectMapper.readTree(jsonText);
              if (arr.isArray()) {
                List<Map<String, String>> result = new ArrayList<>();
                for (JsonNode node : arr) {
                  String name = getStringValue(node, "name");
                  String website = getStringValue(node, "website");
                  if (!name.isEmpty()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", name);
                    map.put("website", website);
                    result.add(map);
                  }
                }
                return result;
              }
            } catch (Exception e) {
              log.error("Failed to parse company list JSON: {}", jsonText, e);
            }
          }
        }
      }
      log.error("Unexpected Gemini API response structure (company list): {}", responseBody);
      return Collections.emptyList();
    } catch (Exception e) {
      log.error("Error parsing Gemini response (company list)", e);
      return Collections.emptyList();
    }
  }

  private String extractJsonFromText(String text) {
    if (text == null || text.trim().isEmpty()) {
      return null;
    }
    text = text.trim();
    int startIndex = text.indexOf('{');
    int endIndex = text.lastIndexOf('}');
    if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
      return text.substring(startIndex, endIndex + 1);
    }
    startIndex = text.indexOf('[');
    endIndex = text.lastIndexOf(']');
    if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
      return text.substring(startIndex, endIndex + 1);
    }
    return null;
  }

  private Map<String, Object> mapJsonToIndustries(JsonNode industryData) {
    Map<String, Object> industries = new HashMap<>();

    try {
      // Handle new format with primary and secondary industries
      if (industryData.has("primaryIndustry") && industryData.has("secondaryIndustries")) {
        String primaryIndustry = getStringValue(industryData, "primaryIndustry");
        List<String> secondaryIndustries = new ArrayList<>();

        JsonNode secondaryArray = industryData.get("secondaryIndustries");
        if (secondaryArray.isArray()) {
          for (JsonNode industryNode : secondaryArray) {
            if (industryNode.isTextual()) {
              secondaryIndustries.add(industryNode.asText());
            }
          }
        }

        industries.put("primaryIndustry", primaryIndustry);
        industries.put("secondaryIndustries", secondaryIndustries);

        // Also maintain backward compatibility with the old "industries" format
        List<String> allIndustries = new ArrayList<>();
        allIndustries.add(primaryIndustry);
        allIndustries.addAll(secondaryIndustries);
        industries.put("industries", allIndustries);

        log.debug("Parsed industry data - Primary: {}, Secondary: {}", primaryIndustry,
            secondaryIndustries);
      } else if (industryData.isArray()) {
        // Handle old array format for backward compatibility
        List<String> detectedIndustries = new ArrayList<>();
        for (JsonNode industryNode : industryData) {
          if (industryNode.isTextual()) {
            detectedIndustries.add(industryNode.asText());
          }
        }
        industries.put("industries", detectedIndustries);

        // If we have industries, treat the first one as primary and rest as secondary
        if (!detectedIndustries.isEmpty()) {
          industries.put("primaryIndustry", detectedIndustries.get(0));
          industries.put("secondaryIndustries",
              detectedIndustries.subList(1, detectedIndustries.size()));
        }
      } else {
        log.warn("Unexpected industry data format: {}", industryData);
      }
    } catch (Exception e) {
      log.error("Error parsing industry data: {}", industryData, e);
    }

    return industries;
  }

  private String getStringValue(JsonNode node, String fieldName) {
    if (node.has(fieldName) && !node.get(fieldName).isNull()) {
      return node.get(fieldName).asText("");
    }
    return "";
  }
} 