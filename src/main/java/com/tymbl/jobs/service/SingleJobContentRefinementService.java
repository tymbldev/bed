package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.service.AIRestService;
import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.repository.ExternalJobDetailRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for refining a single job's content (description and title) using AI with single
 * transaction
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SingleJobContentRefinementService {

  @Autowired
  private AIRestService aiRestService;

  @Autowired
  private ExternalJobDetailRepository externalJobDetailRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Refine both description and title for a single external job detail with single transaction
   */
  @Transactional
  public void refineJobContent(ExternalJobDetail externalJob, String designation) {
    try {
      // Refine description
      String refinedDescription = refineJobDescription(externalJob.getJobDescription());
      externalJob.setRefinedDescription(refinedDescription);

      // Refine title with designation
      String refinedTitle = refineJobTitle(externalJob.getJobTitle(), designation);
      externalJob.setRefinedTitle(refinedTitle);

      // Mark as refined
      externalJob.setIsRefined(true);

      // Save the refined job in the same transaction
      externalJobDetailRepository.save(externalJob);

      log.info("✅ Successfully refined and saved content for external job ID: {}",
          externalJob.getId());
    } catch (Exception e) {
      log.error("Error refining content for external job ID {}: {}",
          externalJob.getId(), e.getMessage(), e);
      // Don't mark as refined if there was an error
      externalJob.setIsRefined(false);
      throw e; // Re-throw to rollback transaction
    }
  }

  /**
   * Refine job description using AI
   */
  private String refineJobDescription(String rawDescription) {
    if (rawDescription == null || rawDescription.trim().isEmpty()) {
      return rawDescription;
    }

    try {
      String prompt = buildDescriptionRefinementPrompt(rawDescription);
      String aiResponse = callGenAIService(prompt);

      if (aiResponse != null && !aiResponse.trim().isEmpty()) {
        // Clean the AI response
        String refinedDescription = cleanAIResponse(aiResponse);
        log.info("✅ Successfully refined job description. Original length: {}, Refined length: {}",
            rawDescription.length(), refinedDescription.length());
        return refinedDescription;
      } else {
        log.warn("AI service returned empty response for description refinement");
        return rawDescription;
      }
    } catch (Exception e) {
      log.error("Error refining job description: {}", e.getMessage(), e);
      return rawDescription; // Return original if refinement fails
    }
  }

  /**
   * Refine job title using AI with both title and designation
   */
  private String refineJobTitle(String rawTitle, String designation) {
    if (rawTitle == null || rawTitle.trim().isEmpty()) {
      return rawTitle;
    }

    try {
      String prompt = buildTitleRefinementPrompt(rawTitle, designation);
      String aiResponse = callGenAIService(prompt);

      if (aiResponse != null && !aiResponse.trim().isEmpty()) {
        // Clean the AI response
        String refinedTitle = cleanAIResponse(aiResponse);
              log.info("✅ Successfully refined job title. Original: '{}', Designation: '{}', Refined: '{}'",
          rawTitle, designation, refinedTitle);
        return refinedTitle;
      } else {
        log.warn("AI service returned empty response for title refinement");
        return rawTitle;
      }
    } catch (Exception e) {
      log.error("Error refining job title: {}", e.getMessage(), e);
      return rawTitle; // Return original if refinement fails
    }
  }

  /**
   * Build prompt for description refinement
   */
  private String buildDescriptionRefinementPrompt(String rawDescription) {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "You are a job description HTML refinement expert. Your task is to clean and improve a job description while maintaining valid HTML structure.\n\n");
    prompt.append("RAW JOB DESCRIPTION HTML:\n");
    prompt.append(rawDescription);
    prompt.append("\n\nREFINEMENT INSTRUCTIONS:\n");
    prompt.append("1. Keep all HTML tags but clean and validate them\n");
    prompt.append(
        "2. Remove unnecessary content like 'show more', 'show less', 'read more', 'click here', etc.\n");
    prompt.append("3. Remove any crawling artifacts or portal-specific content\n");
    prompt.append("4. Clean up any malformed HTML tags or broken elements\n");
    prompt.append("5. Ensure the HTML is valid and well-structured\n");
    prompt.append("6. Keep all relevant job requirements, responsibilities, and qualifications\n");
    prompt.append("7. Maintain the original meaning and intent\n");
    prompt.append("8. Remove any duplicate or redundant information\n");
    prompt.append("9. Ensure proper HTML formatting and indentation\n");
    prompt.append("10. Keep the description concise but comprehensive\n");
    prompt.append("11. Preserve important formatting like lists, paragraphs, and emphasis\n");
    prompt.append("12. Remove any JavaScript code or event handlers\n");
    prompt.append("13. Clean up any CSS classes that are not needed\n");
    prompt.append("14. Ensure proper nesting of HTML elements\n");
    prompt.append("15. Remove any tracking pixels or analytics code\n\n");
    prompt.append(
        "Provide ONLY the refined HTML description without any additional text, explanations, or formatting.\n");
    prompt.append(
        "Do NOT wrap the output in markdown code blocks (```html) or any other formatting.\n");
    prompt.append(
        "The output should be clean, valid HTML that can be directly used in a job posting.\n");
    prompt.append(
        "Maintain semantic HTML structure with proper tags like <p>, <ul>, <li>, <strong>, <em>, etc.\n\n");
    prompt.append("REFINED HTML DESCRIPTION:");

    return prompt.toString();
  }

  /**
   * Build prompt for title refinement
   */
  private String buildTitleRefinementPrompt(String rawTitle, String designation) {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "You are a job title refinement expert. Your task is to improve a job title by incorporating the designation information.\n\n");
    prompt.append("RAW JOB TITLE:\n");
    prompt.append(rawTitle);
    prompt.append("\n\nDESIGNATION:\n");
    prompt.append(designation != null ? designation : "Not specified");
    prompt.append("\n\nREFINEMENT INSTRUCTIONS:\n");
    prompt.append("1. Incorporate the designation into the title if it's not already present\n");
    prompt.append("2. Ensure the title includes the designation/role clearly\n");
    prompt.append(
        "3. Remove any portal-specific prefixes or suffixes (e.g., '(IND)', '(Remote)', etc.)\n");
    prompt.append("4. Remove any location indicators that are not part of the actual title\n");
    prompt.append("5. Ensure proper capitalization and formatting\n");
    prompt.append("6. Make the title professional and industry-standard\n");
    prompt.append("7. Include seniority level if mentioned (e.g., Senior, Junior, Lead, etc.)\n");
    prompt.append("8. Ensure the title accurately reflects the job role and designation\n");
    prompt.append("9. Remove any unnecessary abbreviations or acronyms\n");
    prompt.append("10. Keep the title concise but descriptive\n");
    prompt.append("11. Maintain consistency with standard job title conventions\n");
    prompt.append(
        "12. If the designation is more specific than the title, use the designation as the primary role\n");
    prompt.append("13. Combine title and designation information intelligently\n\n");
    prompt.append(
        "Provide ONLY the refined title without any additional text, explanations, or formatting.\n");
    prompt.append(
        "The output should be a clean, professional job title that incorporates the designation.\n\n");
    prompt.append("REFINED TITLE:");

    return prompt.toString();
  }

  /**
   * Clean AI response by removing common artifacts and formatting
   */
  private String cleanAIResponse(String aiResponse) {
    if (aiResponse == null) {
      return null;
    }

    String cleaned = aiResponse.trim();

    // Remove markdown code blocks (```html ... ```)
    cleaned = cleaned.replaceAll("^```html\\s*", "");
    cleaned = cleaned.replaceAll("^```\\s*", "");
    cleaned = cleaned.replaceAll("\\s*```$", "");

    // Remove common AI response artifacts
    cleaned = cleaned.replaceAll("^Here's the refined.*?:\\s*", "");
    cleaned = cleaned.replaceAll("^The refined.*?:\\s*", "");
    cleaned = cleaned.replaceAll("^Refined.*?:\\s*", "");
    cleaned = cleaned.replaceAll("^Output.*?:\\s*", "");

    // Remove quotes if the entire response is wrapped in them
    if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() > 2) {
      cleaned = cleaned.substring(1, cleaned.length() - 1);
    }

    // Remove any trailing punctuation that might be AI artifacts
    cleaned = cleaned.replaceAll("\\s*[.!?]+\\s*$", "");

    return cleaned.trim();
  }

  /**
   * Call GenAI service to get response
   */
  private String callGenAIService(String prompt) {
    try {
      // Use AIRestService to call Gemini API
      if (aiRestService != null) {
        // Build request body using AIRestService
        Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

        // Call Gemini API
        ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
            "Job Content Refinement");

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
          // Parse the response to extract the generated text
          return extractTextFromGeminiResponse(response.getBody());
        } else {
          log.warn("Gemini API call failed with status: {} - {}", response.getStatusCode(),
              response.getBody());
        }
      }

      // Fallback: Return original content if AI service is not available
      log.info("🤖 AI service not available, returning original content");
      return null;

    } catch (Exception e) {
      log.warn("Failed to call GenAI service: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Extract text from Gemini API response
   */
  private String extractTextFromGeminiResponse(String responseBody) {
    try {
      // Parse JSON response
      JsonNode responseNode = objectMapper.readTree(responseBody);

      // Navigate to the text content
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            log.debug("Extracted text from Gemini response: {}", generatedText);
            return generatedText;
          }
        }
      }

      log.warn("Unexpected Gemini API response structure: {}", responseBody);
      return null;

    } catch (Exception e) {
      log.warn("Failed to parse Gemini response: {}", e.getMessage());
      return null;
    }
  }
}
