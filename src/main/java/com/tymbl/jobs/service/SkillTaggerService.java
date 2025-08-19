package com.tymbl.jobs.service;

import com.tymbl.common.entity.Skill;
import com.tymbl.common.entity.SimilarContent;
import com.tymbl.common.entity.SimilarContent.ContentType;
import com.tymbl.common.repository.SkillRepository;
import com.tymbl.common.repository.SimilarContentRepository;
import com.tymbl.common.service.AIRestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class SkillTaggerService {

  private final SkillRepository skillRepository;
  private final SimilarContentRepository similarContentRepository;
  private final AIRestService aiRestService;
  private final ObjectMapper objectMapper;

  /**
   * Tag skills from JSON data using exact match, like operator, and similar content
   */
  @Transactional
  public SkillTaggingResult tagSkills(String skillsJson, Long sourceId, String portalName) {
    SkillTaggingResult result = new SkillTaggingResult();
    
    if (skillsJson == null || skillsJson.trim().isEmpty()) {
      return result;
    }
    
    try {
      JsonNode skillsArray = objectMapper.readTree(skillsJson);
      
      if (skillsArray.isArray()) {
        List<Long> skillIds = new ArrayList<>();
        List<String> skillNames = new ArrayList<>();
        double totalConfidence = 0.0;
        int processedSkills = 0;
        
        for (JsonNode skillNode : skillsArray) {
          if (skillNode.has("text")) {
            String skillName = skillNode.get("text").asText();
            if (skillName != null && !skillName.trim().isEmpty()) {
              skillNames.add(skillName);
              
              // Tag individual skill using the same pattern as designation
              SkillTaggingResult individualResult = tagIndividualSkill(skillName, sourceId, portalName);
              if (individualResult.getSkillId() != null) {
                skillIds.add(individualResult.getSkillId());
                totalConfidence += individualResult.getConfidence();
              } else {
                totalConfidence += 0.5; // Lower confidence for new skills
              }
              processedSkills++;
            }
          }
        }
        
        result.setSkillIds(skillIds);
        result.setSkillNames(skillNames);
        result.setConfidence(processedSkills > 0 ? totalConfidence / processedSkills : 0.0);
        
        log.info("Tagged {} skills for external job {}: {}", skillIds.size(), sourceId, skillNames);
      }
      
    } catch (Exception e) {
      log.error("Error tagging skills for external job {}: {}", sourceId, e.getMessage(), e);
      result.setError(e.getMessage());
    }
    
    return result;
  }

  /**
   * Tag individual skill using the same pattern as designation tagger
   */
  private SkillTaggingResult tagIndividualSkill(String skillName, Long sourceId, String portalName) {
    if (skillName == null || skillName.trim().isEmpty()) {
      return new SkillTaggingResult();
    }

    String normalizedSkillName = skillName.trim();

    // 1. Try exact match first
    Optional<Skill> exactMatch = skillRepository.findByNameIgnoreCase(normalizedSkillName);
    if (exactMatch.isPresent()) {
      Skill skill = exactMatch.get();
      // Update usage count
      skill.setUsageCount(skill.getUsageCount() + 1);
      skillRepository.save(skill);
      return new SkillTaggingResult(skill.getId(), skill.getName(), 1.0);
    }

    // 2. Try similar content table
    List<SimilarContent> similarSkills = similarContentRepository.findByTypeAndSearchTerm(
        ContentType.SKILL, normalizedSkillName);
    
    if (!similarSkills.isEmpty()) {
      // Find the best match by confidence score
      SimilarContent bestMatch = similarSkills.stream()
          .filter(sc -> sc.getSimilarName().equalsIgnoreCase(normalizedSkillName))
          .max((a, b) -> a.getConfidenceScore().compareTo(b.getConfidenceScore()))
          .orElse(null);
      
      if (bestMatch != null) {
        Optional<Skill> skill = skillRepository.findByNameIgnoreCase(bestMatch.getParentName());
        if (skill.isPresent()) {
          Skill foundSkill = skill.get();
          // Update usage count
          foundSkill.setUsageCount(foundSkill.getUsageCount() + 1);
          skillRepository.save(foundSkill);
          return new SkillTaggingResult(
              foundSkill.getId(), 
              foundSkill.getName(), 
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 3. Try LIKE operator on skills table
    List<Skill> likeMatches = skillRepository.findByNameContainingIgnoreCase(normalizedSkillName);
    if (!likeMatches.isEmpty()) {
      // Use AI to find the best match
      Skill bestMatch = findBestSkillMatchUsingAI(normalizedSkillName, likeMatches);
      if (bestMatch != null) {
        // Store this mapping in similar content for future use
        storeSkillMapping(bestMatch.getName(), normalizedSkillName, 0.85);
        // Update usage count
        bestMatch.setUsageCount(bestMatch.getUsageCount() + 1);
        skillRepository.save(bestMatch);
        return new SkillTaggingResult(bestMatch.getId(), bestMatch.getName(), 0.85);
      }
    }

    // 4. Try GenAI with all skills as fallback
    Skill genAIMatch = findBestSkillUsingGenAI(normalizedSkillName);
    if (genAIMatch != null) {
      // Update usage count
      genAIMatch.setUsageCount(genAIMatch.getUsageCount() + 1);
      skillRepository.save(genAIMatch);
      return new SkillTaggingResult(genAIMatch.getId(), genAIMatch.getName(), 0.7);
    }

    // 5. Create new skill if no match found
    Skill newSkill = createNewSkill(normalizedSkillName);
    if (newSkill != null) {
      return new SkillTaggingResult(newSkill.getId(), newSkill.getName(), 0.5);
    }

    // 6. Log NO_MATCH case for future analysis
    log.info("NO_MATCH for skill: '{}' (sourceId: {}, portal: {}) - No match found after all tagging strategies", 
        normalizedSkillName, sourceId, portalName);

    return new SkillTaggingResult();
  }

  /**
   * Find best skill match using AI
   */
  private Skill findBestSkillMatchUsingAI(String skillName, List<Skill> skills) {
    try {
      StringBuilder prompt = new StringBuilder();
      prompt.append("You are a skill matching expert. Match the input skill to the best available skill.\n\n");
      prompt.append("Input skill: '").append(skillName).append("'\n\n");
      prompt.append("Available skills:\n");
      
      for (int i = 0; i < skills.size(); i++) {
        Skill skill = skills.get(i);
        prompt.append(i + 1).append(". ").append(skill.getName()).append("\n");
      }
      
      prompt.append("\nRespond with the EXACT skill name from the list above, or 'NO_MATCH' if none are suitable.\n");
      prompt.append("Only respond with the exact skill name or 'NO_MATCH', no additional text.");

      String aiResponse = callGenAIService(prompt.toString());
      
      if (aiResponse != null && !aiResponse.trim().equalsIgnoreCase("NO_MATCH")) {
        String selectedSkillName = aiResponse.trim();
        
        for (Skill skill : skills) {
          if (skill.getName().equalsIgnoreCase(selectedSkillName)) {
            return skill;
          }
        }
      }
      
    } catch (Exception e) {
      log.warn("Error in AI skill matching: {}", e.getMessage());
    }
    
    return null;
  }

  /**
   * Find best skill using GenAI with all skills
   */
  private Skill findBestSkillUsingGenAI(String skillName) {
    try {
      // Get top skills by usage count for better matching
      List<Skill> topSkills = skillRepository.findTop20ByOrderByUsageCountDesc();
      
      StringBuilder prompt = new StringBuilder();
      prompt.append("You are a skill matching expert. Match the input skill to the best available skill.\n\n");
      prompt.append("Input skill: '").append(skillName).append("'\n\n");
      prompt.append("Available skills (top 20 by usage):\n");
      
      for (int i = 0; i < topSkills.size(); i++) {
        Skill skill = topSkills.get(i);
        prompt.append(i + 1).append(". ").append(skill.getName()).append("\n");
      }
      
      prompt.append("\nRespond with the EXACT skill name from the list above, or 'NO_MATCH' if none are suitable.\n");
      prompt.append("Only respond with the exact skill name or 'NO_MATCH', no additional text.");

      String aiResponse = callGenAIService(prompt.toString());
      
      if (aiResponse != null && !aiResponse.trim().equalsIgnoreCase("NO_MATCH")) {
        String selectedSkillName = aiResponse.trim();
        
        for (Skill skill : topSkills) {
          if (skill.getName().equalsIgnoreCase(selectedSkillName)) {
            return skill;
          }
        }
      }
      
    } catch (Exception e) {
      log.warn("Error in GenAI skill matching: {}", e.getMessage());
    }
    
    return null;
  }

  /**
   * Store skill mapping in similar content table
   */
  private void storeSkillMapping(String parentSkillName, String similarSkillName, double confidence) {
    try {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setType(ContentType.SKILL);
      similarContent.setParentName(parentSkillName);
      similarContent.setSimilarName(similarSkillName);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setProcessed(true);
      
      similarContentRepository.save(similarContent);
      log.debug("Stored skill mapping: '{}' -> '{}' with confidence {}", similarSkillName, parentSkillName, confidence);
      
    } catch (Exception e) {
      log.warn("Failed to store skill mapping: {}", e.getMessage());
    }
  }

  /**
   * Create new skill if not found
   */
  private Skill createNewSkill(String skillName) {
    try {
      Skill newSkill = new Skill();
      newSkill.setName(skillName);
      newSkill.setDescription("Auto-generated from external job data");
      newSkill.setCategory("Technical");
      newSkill.setEnabled(true);
      newSkill.setUsageCount(1L);
      
      Skill savedSkill = skillRepository.save(newSkill);
      log.info("Created new skill: {} with ID: {}", skillName, savedSkill.getId());
      return savedSkill;
      
    } catch (Exception e) {
      log.error("Error creating new skill: {}", skillName, e);
      return null;
    }
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
        ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody, "Skill Matching");
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
          // Parse the response to extract the generated text
          return extractTextFromGeminiResponse(response.getBody());
        } else {
          log.warn("Gemini API call failed with status: {} - {}", response.getStatusCode(), response.getBody());
        }
      }
      
      // Fallback: Return null if AI service is not available
      log.info("AI service not available for skill matching");
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

  /**
   * Result class for skill tagging
   */
  public static class SkillTaggingResult {
    private Long skillId;
    private List<Long> skillIds = new ArrayList<>();
    private List<String> skillNames = new ArrayList<>();
    private Double confidence = 0.0;
    private String error;

    // Constructors
    public SkillTaggingResult() {}
    
    public SkillTaggingResult(Long skillId, String skillName, Double confidence) {
      this.skillId = skillId;
      this.confidence = confidence;
    }

    // Getters and setters
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    
    public List<Long> getSkillIds() { return skillIds; }
    public void setSkillIds(List<Long> skillIds) { this.skillIds = skillIds; }
    
    public List<String> getSkillNames() { return skillNames; }
    public void setSkillNames(List<String> skillNames) { this.skillNames = skillNames; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
  }
}
