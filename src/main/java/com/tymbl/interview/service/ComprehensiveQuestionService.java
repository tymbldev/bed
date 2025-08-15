package com.tymbl.interview.service;

import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Skill;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.SkillRepository;
import com.tymbl.common.service.GeminiService;
import com.tymbl.interview.entity.DesignationSkillQuestionMapping;
import com.tymbl.interview.entity.InterviewQuestion;
import com.tymbl.interview.repository.DesignationSkillQuestionMappingRepository;
import com.tymbl.interview.repository.InterviewQuestionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComprehensiveQuestionService {

  private final SkillRepository skillRepository;
  private final DesignationRepository designationRepository;
  private final InterviewQuestionRepository interviewQuestionRepository;
  private final DesignationSkillQuestionMappingRepository mappingRepository;
  private final GeminiService geminiService;

  @Transactional
  public Map<String, Object> generateQuestionsForAllSkills() {
    log.info("Starting comprehensive question generation for all skills");

    Map<String, Object> result = new HashMap<>();
    List<Map<String, Object>> skillResults = new ArrayList<>();
    int totalQuestionsGenerated = 0;
    int totalSkillsProcessed = 0;

    // Get all enabled skills
    List<Skill> skills = skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc();
    log.info("Found {} enabled skills to process", skills.size());

    // Create a fixed thread pool with 10 threads for parallel processing
    ExecutorService executor = Executors.newFixedThreadPool(10);

    try {
      // Process skills in parallel using CompletableFuture
      List<CompletableFuture<Map<String, Object>>> futures = skills.stream()
          .map(skill -> CompletableFuture.supplyAsync(() -> {
            try {
              log.info("Processing skill: {} (ID: {})", skill.getName(), skill.getId());
              return generateQuestionsForSkill(skill);
            } catch (Exception e) {
              log.error("Error processing skill: {}", skill.getName(), e);
              Map<String, Object> errorResult = new HashMap<>();
              errorResult.put("skill_name", skill.getName());
              errorResult.put("skill_id", skill.getId());
              errorResult.put("error", e.getMessage());
              errorResult.put("questions_generated", 0);
              return errorResult;
            }
          }, executor))
          .collect(Collectors.toList());

      // Wait for all futures to complete and collect results
      for (CompletableFuture<Map<String, Object>> future : futures) {
        try {
          Map<String, Object> skillResult = future.get();
          skillResults.add(skillResult);

          int questionsGenerated = (Integer) skillResult.get("questions_generated");
          totalQuestionsGenerated += questionsGenerated;
          totalSkillsProcessed++;

        } catch (Exception e) {
          log.error("Error waiting for skill processing to complete", e);
        }
      }

    } finally {
      // Shutdown the executor
      executor.shutdown();
    }

    result.put("total_skills_processed", totalSkillsProcessed);
    result.put("total_questions_generated", totalQuestionsGenerated);
    result.put("skill_results", skillResults);
    result.put("message", "Comprehensive question generation completed with parallel processing");

    log.info("Completed comprehensive question generation. Total questions: {}",
        totalQuestionsGenerated);
    return result;
  }

  @Transactional
  public Map<String, Object> generateQuestionsForSkill(Skill skill) {
    log.info("Generating comprehensive questions for skill: {}", skill.getName());

    Map<String, Object> result = new HashMap<>();
    result.put("skill_name", skill.getName());
    result.put("skill_id", skill.getId());

    try {
      // Step 1: Generate 30 summary questions
      List<Map<String, Object>> summaryQuestions = geminiService.generateComprehensiveInterviewQuestions(
          skill.getName(), 100);

      if (summaryQuestions.isEmpty()) {
        log.warn("No summary questions generated for skill: {}", skill.getName());
        result.put("questions_generated", 0);
        result.put("error", "No summary questions generated");
        return result;
      }

      log.info("Generated {} summary questions for skill: {}", summaryQuestions.size(),
          skill.getName());

      // Step 2: Generate detailed content for each summary question
      int questionsGenerated = 0;
      List<DesignationSkillQuestionMapping> savedMappings = new ArrayList<>();

      for (Map<String, Object> summaryQuestion : summaryQuestions) {
        try {
          // Generate detailed content
          String questionText = (String) summaryQuestion.get("question");
          List<Map<String, Object>> detailedContent = geminiService.generateDetailedQuestionContent(
              skill.getName(), questionText);

          if (!detailedContent.isEmpty()) {
            Map<String, Object> content = detailedContent.get(0);

            // Create and save InterviewQuestion
            InterviewQuestion question = InterviewQuestion.builder()
                .skillId(skill.getId())
                .skillName(skill.getName())
                .question(questionText)
                .summaryAnswer((String) summaryQuestion.get("summary_answer"))
                .answer((String) content.get("detailed_answer"))
                .difficultyLevel((String) summaryQuestion.get("difficulty_level"))
                .questionType((String) summaryQuestion.get("question_type"))
                .tags((String) content.get("tags"))
                .htmlContent((String) content.get("html_content"))
                .codeExamples((String) content.get("code_examples"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            InterviewQuestion savedQuestion = interviewQuestionRepository.save(
                question); // Save one by one
            questionsGenerated++;

            // Create designation mappings
            @SuppressWarnings("unchecked")
            List<String> applicableDesignations = (List<String>) summaryQuestion.get(
                "applicable_designations");
            if (applicableDesignations != null) {
              for (String designationName : applicableDesignations) {
                Designation designation = designationRepository.findByName(designationName)
                    .orElse(null);
                if (designation != null) {
                  DesignationSkillQuestionMapping mapping = DesignationSkillQuestionMapping.builder()
                      .designationId(designation.getId())
                      .designationName(designation.getName())
                      .skillId(skill.getId())
                      .skillName(skill.getName())
                      .questionId(savedQuestion.getId())
                      .relevanceScore(1.0)
                      .createdAt(LocalDateTime.now())
                      .build();

                  savedMappings.add(mapping);
                }
              }
            }

            // Add delay to avoid rate limiting
            //  Thread.sleep(1000);
          }

        } catch (Exception e) {
          log.error("Error processing question for skill {}: {}", skill.getName(),
              summaryQuestion.get("question"), e);
        }
      }

      // Save all mappings
      if (!savedMappings.isEmpty()) {
        mappingRepository.saveAll(savedMappings);
      }

      result.put("questions_generated", questionsGenerated);
      result.put("mappings_created", savedMappings.size());
      result.put("status", "success");

      log.info("Successfully generated {} questions for skill: {}", questionsGenerated,
          skill.getName());

    } catch (Exception e) {
      log.error("Error generating questions for skill: {}", skill.getName(), e);
      result.put("questions_generated", 0);
      result.put("error", e.getMessage());
    }

    return result;
  }

  @Transactional
  public Map<String, Object> generateQuestionsForSpecificSkill(String skillName) {
    log.info("Generating comprehensive questions for specific skill: {}", skillName);

    Skill skill = skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc()
        .stream()
        .filter(s -> s.getName().equalsIgnoreCase(skillName))
        .findFirst()
        .orElse(null);

    if (skill == null) {
      Map<String, Object> errorResult = new HashMap<>();
      errorResult.put("error", "Skill not found: " + skillName);
      return errorResult;
    }

    return generateQuestionsForSkill(skill);
  }
} 