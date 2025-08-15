package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Skill;
import com.tymbl.common.repository.SkillRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillSimilarSkillsService {

  private final AIRestService aiRestService;
  private final SkillRepository skillRepository;
  private final ObjectMapper objectMapper;

  /**
   * Generate similar skills for all skills using GenAI
   */
  @Transactional
  public Map<String, Object> generateSimilarSkillsForAllSkills() {
    try {
      log.info("Starting similar skills generation for all skills using GenAI");

      // Get all skills that don't have similar skills processed
      List<Skill> skillsToProcess = skillRepository.findAll().stream()
          .filter(skill -> !skill.isSimilarSkillsProcessed())
          .collect(Collectors.toList());

      if (skillsToProcess.isEmpty()) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "All skills already have similar skills generated");
        result.put("processed", 0);
        result.put("failed", 0);
        result.put("total", skillRepository.count());
        return result;
      }

      // Get all available skills for reference
      List<Skill> allSkills = skillRepository.findAll();
      List<String> availableSkillNames = allSkills.stream()
          .map(Skill::getName)
          .collect(Collectors.toList());

      int processed = 0;
      int failed = 0;
      List<String> errors = new ArrayList<>();

      for (Skill skill : skillsToProcess) {
        try {
          boolean success = generateSimilarSkillsForSkill(skill, availableSkillNames);
          if (success) {
            processed++;
            log.info("Successfully generated similar skills for: {}", skill.getName());
          } else {
            failed++;
            errors.add("Failed to generate similar skills for: " + skill.getName());
            log.warn("Failed to generate similar skills for: {}", skill.getName());
          }
        } catch (Exception e) {
          failed++;
          errors.add(
              "Error generating similar skills for: " + skill.getName() + " - " + e.getMessage());
          log.error("Error generating similar skills for: {}", skill.getName(), e);
        }
      }

      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Similar skills generation completed");
      result.put("processed", processed);
      result.put("failed", failed);
      result.put("total", skillsToProcess.size());
      if (!errors.isEmpty()) {
        result.put("errors", errors);
      }

      log.info("Similar skills generation completed - Success: {}, Failed: {}", processed, failed);
      return result;

    } catch (Exception e) {
      log.error("Error generating similar skills for all skills", e);
      Map<String, Object> error = new HashMap<>();
      error.put("success", false);
      error.put("error", "Error generating similar skills: " + e.getMessage());
      return error;
    }
  }

  /**
   * Generate similar skills for a specific skill using GenAI
   */
  @Transactional
  public Map<String, Object> generateSimilarSkillsForSkill(String skillName) {
    try {
      log.info("Generating similar skills for skill: {}", skillName);

      Skill skill = skillRepository.findByNameIgnoreCase(skillName).orElse(null);
      if (skill == null) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", "Skill not found: " + skillName);
        return error;
      }

      if (skill.isSimilarSkillsProcessed()) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Similar skills already generated for this skill");
        result.put("skill", skill.getName());
        result.put("similarSkills", skill.getSimilarSkillsByName());
        return result;
      }

      // Get all available skills for reference
      List<Skill> allSkills = skillRepository.findAll();
      List<String> availableSkillNames = allSkills.stream()
          .map(Skill::getName)
          .collect(Collectors.toList());

      boolean success = generateSimilarSkillsForSkill(skill, availableSkillNames);

      if (success) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("skill", skill.getName());
        result.put("skillId", skill.getId());
        result.put("similarSkills", skill.getSimilarSkillsByName());
        result.put("message", "Similar skills generated successfully");
        return result;
      } else {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", "Failed to generate similar skills for: " + skillName);
        return error;
      }

    } catch (Exception e) {
      log.error("Error generating similar skills for skill: {}", skillName, e);
      Map<String, Object> error = new HashMap<>();
      error.put("success", false);
      error.put("error", "Error generating similar skills: " + e.getMessage());
      return error;
    }
  }

  /**
   * Internal method to generate similar skills for a skill using GenAI
   */
  private boolean generateSimilarSkillsForSkill(Skill skill, List<String> availableSkillNames) {
    try {
      String prompt = buildSimilarSkillsPrompt(skill.getName(), availableSkillNames);

      Map<String, Object> requestBody = new HashMap<>();
      Map<String, Object> part = new HashMap<>();
      part.put("text", prompt);
      List<Object> parts = Arrays.asList(part);
      Map<String, Object> content = new HashMap<>();
      content.put("parts", parts);
      List<Object> contents = Arrays.asList(content);
      requestBody.put("contents", contents);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Generate similar skills for: " + skill.getName());

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        String similarSkillsJson = parseSimilarSkillsResponse(response.getBody(),
            availableSkillNames);

        if (similarSkillsJson != null && !similarSkillsJson.trim().isEmpty()) {
          // Update the skill with similar skills
          skill.setSimilarSkillsByName(similarSkillsJson);
          skill.setSimilarSkillsProcessed(true);
          skillRepository.save(skill);

          log.info("Successfully generated similar skills for {}: {}", skill.getName(),
              similarSkillsJson);
          return true;
        }
      }

      log.warn("Failed to generate similar skills for: {}", skill.getName());
      return false;

    } catch (Exception e) {
      log.error("Error generating similar skills for skill: {}", skill.getName(), e);
      return false;
    }
  }

  /**
   * Build prompt for generating similar skills
   */
  private String buildSimilarSkillsPrompt(String skillName, List<String> availableSkillNames) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You are an expert in technology and programming skills analysis. ");
    prompt.append("I need you to identify the most similar and related skills to a given skill. ");
    prompt.append("The similar skills should be from the list of available skills I provide.\n\n");

    prompt.append("Available skills in the system:\n");
    for (String skill : availableSkillNames) {
      prompt.append("- ").append(skill).append("\n");
    }

    prompt.append("\nTarget skill: ").append(skillName).append("\n\n");
    prompt.append(
        "Please analyze the target skill and identify ALL the most similar and related skills from the available list above. ");
    prompt.append("Consider:\n");
    prompt.append("1. Direct technical relationships (e.g., Java → Spring, Hibernate, Maven)\n");
    prompt.append("2. Complementary skills (e.g., Frontend → Backend, Database → ORM)\n");
    prompt.append("3. Related technologies in the same ecosystem\n");
    prompt.append("4. Skills that are commonly used together\n");
    prompt.append("5. Skills that share similar concepts or paradigms\n\n");

    prompt.append(
        "Return ONLY a JSON array of skill names (exactly as they appear in the available list above), nothing else.\n");
    prompt.append(
        "Make the list extensive and comprehensive - include as many relevant skills as possible.\n\n");

    prompt.append("Examples:\n");
    prompt.append(
        "- For 'Java': [\"Spring\", \"Hibernate\", \"Maven\", \"JUnit\", \"JPA\", \"JSP\", \"Servlets\", \"J2EE\", \"Android\", \"Kotlin\", \"Scala\", \"Groovy\"]\n");
    prompt.append(
        "- For 'JavaScript': [\"React\", \"Node.js\", \"TypeScript\", \"Angular\", \"Vue.js\", \"jQuery\", \"Express.js\", \"MongoDB\", \"HTML\", \"CSS\"]\n");
    prompt.append(
        "- For 'Python': [\"Django\", \"Flask\", \"Pandas\", \"NumPy\", \"Matplotlib\", \"Scikit-learn\", \"TensorFlow\", \"PyTorch\", \"Jupyter\", \"FastAPI\"]\n\n");

    prompt.append("Now generate similar skills for: ").append(skillName);

    return prompt.toString();
  }

  /**
   * Parse the response from Gemini API to extract similar skills
   */
  private String parseSimilarSkillsResponse(String responseBody, List<String> availableSkillNames) {
    try {
      JsonNode responseNode = objectMapper.readTree(responseBody);
      JsonNode candidates = responseNode.path("candidates");

      if (candidates.isArray() && candidates.size() > 0) {
        JsonNode firstCandidate = candidates.get(0);
        JsonNode content = firstCandidate.path("content");
        JsonNode parts = content.path("parts");

        if (parts.isArray() && parts.size() > 0) {
          JsonNode firstPart = parts.get(0);
          String text = firstPart.path("text").asText();

          if (text != null && !text.trim().isEmpty()) {
            // Try to extract JSON array from the response
            String jsonArray = extractJsonArray(text);
            if (jsonArray != null) {
              // Validate that all skills in the array exist in available skills
              List<String> similarSkills = parseJsonArray(jsonArray);
              List<String> validSkills = similarSkills.stream()
                  .filter(skill -> availableSkillNames.contains(skill))
                  .collect(Collectors.toList());

              if (!validSkills.isEmpty()) {
                return String.join(", ", validSkills);
              }
            }
          }
        }
      }

      log.warn("Could not parse similar skills from Gemini response: {}", responseBody);
      return null;

    } catch (Exception e) {
      log.error("Error parsing similar skills response: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Extract JSON array from text response
   */
  private String extractJsonArray(String text) {
    try {
      // Look for JSON array pattern [skill1, skill2, skill3]
      int startIndex = text.indexOf('[');
      int endIndex = text.lastIndexOf(']');

      if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
        return text.substring(startIndex, endIndex + 1);
      }

      return null;
    } catch (Exception e) {
      log.warn("Error extracting JSON array from text: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Parse JSON array string to list of skills
   */
  private List<String> parseJsonArray(String jsonArray) {
    try {
      JsonNode arrayNode = objectMapper.readTree(jsonArray);
      if (arrayNode.isArray()) {
        List<String> skills = new ArrayList<>();
        for (JsonNode skillNode : arrayNode) {
          if (skillNode.isTextual()) {
            skills.add(skillNode.asText().trim());
          }
        }
        return skills;
      }
    } catch (Exception e) {
      log.warn("Error parsing JSON array: {}", e.getMessage());
    }
    return new ArrayList<>();
  }

  /**
   * Get similar skills generation statistics
   */
  public Map<String, Object> getSimilarSkillsGenerationStats() {
    try {
      long totalSkills = skillRepository.count();
      long processedSkills = skillRepository.findAll().stream()
          .filter(Skill::isSimilarSkillsProcessed)
          .count();
      long unprocessedSkills = totalSkills - processedSkills;
      double processingPercentage =
          totalSkills > 0 ? (double) processedSkills / totalSkills * 100 : 0;

      Map<String, Object> stats = new HashMap<>();
      stats.put("totalSkills", totalSkills);
      stats.put("processedSkills", processedSkills);
      stats.put("unprocessedSkills", unprocessedSkills);
      stats.put("processingPercentage", Math.round(processingPercentage * 100.0) / 100.0);

      return stats;

    } catch (Exception e) {
      log.error("Error getting similar skills generation stats", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting stats: " + e.getMessage());
      return error;
    }
  }

  /**
   * Get skills grouped by similar skills
   */
  public Map<String, List<String>> getSkillsBySimilarSkills() {
    try {
      List<Skill> allSkills = skillRepository.findAll();
      Map<String, List<String>> skillsBySimilar = new HashMap<>();

      for (Skill skill : allSkills) {
        if (skill.isSimilarSkillsProcessed() && skill.getSimilarSkillsByName() != null) {
          String[] similarSkills = skill.getSimilarSkillsByName().split(",");
          List<String> similarSkillsList = Arrays.stream(similarSkills)
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .collect(Collectors.toList());

          if (!similarSkillsList.isEmpty()) {
            skillsBySimilar.put(skill.getName(), similarSkillsList);
          }
        }
      }

      return skillsBySimilar;

    } catch (Exception e) {
      log.error("Error getting skills by similar skills", e);
      return new HashMap<>();
    }
  }
}
