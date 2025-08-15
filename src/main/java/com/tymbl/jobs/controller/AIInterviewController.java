package com.tymbl.jobs.controller;

import com.tymbl.jobs.service.AIJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/ai/interview")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Interview Operations", description = "APIs for AI-powered interview question generation and skill-topic management")
public class AIInterviewController {

  private final AIJobService aiJobService;

  // ============================================================================
  // INTERVIEW QUESTION GENERATION ENDPOINTS
  // ============================================================================

  @PostMapping("/questions/generate-comprehensive")
  @Operation(
      summary = "Generate comprehensive interview questions for all skills",
      description = "Generates detailed interview questions for all skills in the system using AI"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Comprehensive interview questions generated successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"total_skills_processed\": 15,\n" +
                      "  \"total_questions_generated\": 450,\n" +
                      "  \"message\": \"Comprehensive question generation completed\",\n" +
                      "  \"skill_results\": [\n" +
                      "    {\n" +
                      "      \"skill_name\": \"Java\",\n" +
                      "      \"skill_id\": 1,\n" +
                      "      \"questions_generated\": 30,\n" +
                      "      \"mappings_created\": 90,\n" +
                      "      \"status\": \"success\"\n" +
                      "    }\n" +
                      "  ]\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> generateComprehensiveInterviewQuestions() {
    try {
      log.info("Starting comprehensive interview question generation for all skills");
      Map<String, Object> result = aiJobService.generateComprehensiveInterviewQuestions();
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error generating comprehensive interview questions", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error",
          "Error generating comprehensive interview questions: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  @PostMapping("/questions/generate-for-skill/{skillName}")
  @Operation(
      summary = "Generate comprehensive interview questions for specific skill",
      description = "Generates detailed interview questions for a specific skill using AI"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Comprehensive interview questions generated successfully for the skill",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"skill_name\": \"Java\",\n" +
                      "  \"skill_id\": 1,\n" +
                      "  \"questions_generated\": 30,\n" +
                      "  \"mappings_created\": 90,\n" +
                      "  \"status\": \"success\"\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "404", description = "Skill not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> generateComprehensiveInterviewQuestionsForSkill(
      @Parameter(description = "Skill name")
      @PathVariable String skillName) {
    try {
      log.info("Starting comprehensive interview question generation for skill: {}", skillName);
      Map<String, Object> result = aiJobService.generateComprehensiveInterviewQuestionsForSkill(
          skillName);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error generating comprehensive interview questions for skill: {}", skillName, e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error",
          "Error generating comprehensive interview questions for skill: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  // ============================================================================
  // SKILL-TOPIC QUESTION GENERATION ENDPOINTS
  // ============================================================================

  @PostMapping("/skills/{skillName}/topics/{topicName}/questions/generate-and-save")
  @Operation(
      summary = "Generate and save interview questions for a skill and topic",
      description =
          "Uses Gemini to generate summary and detailed, HTML-rich interview questions for a given skill and topic, "
              +
              "and saves them to the InterviewQuestion table. Coding questions get code in Java, Python, and C++."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Questions generated and saved successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"skill_name\": \"Java\",\n" +
                      "  \"topic_name\": \"Collections\",\n" +
                      "  \"questions_added\": 10,\n" +
                      "  \"questions\": [\n" +
                      "    {\"question\": \"Explain Java Collections Framework\", \"answer\": \"<div>...</div>\", \"coding\": true, \"javaCode\": \"...\", \"pythonCode\": \"...\", \"cppCode\": \"...\"}\n"
                      +
                      "  ]\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "404", description = "Skill or topic not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> generateAndSaveQuestionsForSkillAndTopic(
      @Parameter(description = "Skill name")
      @PathVariable String skillName,
      @Parameter(description = "Topic name")
      @PathVariable String topicName,
      @Parameter(description = "Number of questions to generate (default: 10)")
      @RequestParam(defaultValue = "10") int numQuestions) {
    try {
      log.info("Starting question generation for skill: {} and topic: {} with {} questions",
          skillName, topicName, numQuestions);
      Map<String, Object> result = aiJobService.generateAndSaveQuestionsForSkillAndTopic(skillName,
          topicName, numQuestions);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error generating questions for skill: {} and topic: {}", skillName, topicName, e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error",
          "Error generating questions for skill and topic: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  @PostMapping("/skills/{skillName}/topics/questions/generate-and-save")
  @Operation(
      summary = "Generate and save interview questions for all topics of a skill",
      description =
          "Loops through all topics for a skill and generates (and saves) questions for each topic using Gemini. "
              +
              "Uses the same logic as the single-topic endpoint."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Questions generated and saved for all topics successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"skill_name\": \"Java\",\n" +
                      "  \"results\": [\n" +
                      "    {\"topic_name\": \"Collections\", \"questions_added\": 10, \"questions\": [...]},\n"
                      +
                      "    {\"topic_name\": \"Multithreading\", \"questions_added\": 8, \"questions\": [...]}\n"
                      +
                      "  ],\n" +
                      "  \"message\": \"Questions generated and (optionally) saved for all topics\"\n"
                      +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "404", description = "Skill not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> generateAndSaveQuestionsForAllTopicsOfSkill(
      @Parameter(description = "Skill name")
      @PathVariable String skillName,
      @Parameter(description = "Number of questions to generate per topic (default: 10)")
      @RequestParam(defaultValue = "10") int numQuestions) {
    try {
      log.info(
          "Starting question generation for all topics of skill: {} with {} questions per topic",
          skillName, numQuestions);
      Map<String, Object> result = aiJobService.generateAndSaveQuestionsForAllTopicsOfSkill(
          skillName, numQuestions);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error generating questions for all topics of skill: {}", skillName, e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error",
          "Error generating questions for all topics of skill: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  @PostMapping("/skills/topics/questions/generate-and-save-all")
  @Operation(
      summary = "Generate and save interview questions for all skills and topics",
      description =
          "Loops through all enabled skills and all their topics, generates (and saves) questions for each topic using Gemini. "
              +
              "Uses the same logic as the single-skill endpoint."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Questions generated and saved for all skills and topics successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"total_skills\": 15,\n" +
                      "  \"results\": [\n" +
                      "    {\"skill_name\": \"Java\", \"topics_processed\": 8, \"total_questions_added\": 80, \"message\": \"Questions generated and saved for all topics\"},\n"
                      +
                      "    {\"skill_name\": \"Python\", \"topics_processed\": 10, \"total_questions_added\": 100, \"message\": \"Questions generated and saved for all topics\"}\n"
                      +
                      "  ],\n" +
                      "  \"message\": \"Questions generated and saved for all skills and topics\"\n"
                      +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> generateAndSaveQuestionsForAllSkillsAndTopics(
      @Parameter(description = "Number of questions to generate per topic (default: 10)")
      @RequestParam(defaultValue = "10") int numQuestions) {
    try {
      log.info("Starting question generation for all skills and topics with {} questions per topic",
          numQuestions);
      Map<String, Object> result = aiJobService.generateAndSaveQuestionsForAllSkillsAndTopics(
          numQuestions);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error generating questions for all skills and topics", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error",
          "Error generating questions for all skills and topics: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  // ============================================================================
  // SKILL-TOPIC MANAGEMENT ENDPOINTS
  // ============================================================================

  @PostMapping("/skills/{skillName}/topics/generate-and-save")
  @Operation(
      summary = "Generate and save topics for a technical skill",
      description =
          "Uses Gemini to generate topics for a given technical skill and saves them to the SkillTopic table. "
              +
              "Excludes interpersonal/soft skills."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Topics generated and saved successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"skill_name\": \"Java\",\n" +
                      "  \"topics_added\": 8,\n" +
                      "  \"topics\": [\n" +
                      "    {\"topic\": \"Collections\", \"description\": \"Data structures in Java\"}\n"
                      +
                      "  ]\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "404", description = "Skill not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> generateAndSaveTopicsForSkill(
      @Parameter(description = "Skill name")
      @PathVariable String skillName) {
    try {
      log.info("Starting topic generation for skill: {}", skillName);
      Map<String, Object> result = aiJobService.generateAndSaveTopicsForSkill(skillName);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error generating topics for skill: {}", skillName, e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Error generating topics for skill: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  @PostMapping("/skills/topics/generate-and-save-all")
  @Operation(
      summary = "Generate and save topics for all technical skills",
      description =
          "Uses Gemini to generate topics for all enabled technical skills and saves them to the SkillTopic table. "
              +
              "Excludes interpersonal/soft skills."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Topics generated and saved for all skills successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"total_skills\": 15,\n" +
                      "  \"results\": [\n" +
                      "    {\"skill_name\": \"Java\", \"topics_added\": 8, \"message\": \"Topics generated and saved successfully\"},\n"
                      +
                      "    {\"skill_name\": \"Python\", \"topics_added\": 10, \"message\": \"Topics generated and saved successfully\"}\n"
                      +
                      "  ],\n" +
                      "  \"message\": \"Topics generated and saved for all skills\"\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> generateAndSaveTopicsForAllSkills() {
    try {
      log.info("Starting topic generation for all technical skills");
      Map<String, Object> result = aiJobService.generateAndSaveTopicsForAllSkills();
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error generating topics for all skills", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Error generating topics for all skills: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  // ============================================================================
  // SKILL GENERATION ENDPOINTS
  // ============================================================================

  @PostMapping("/skills/generate-and-save")
  @Operation(
      summary = "Generate and save more tech skills using AI",
      description = "Uses Gemini to generate a comprehensive list of tech skills and saves new ones to the Skill table."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Skills generated and saved successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"new_skills_added\": 10,\n" +
                      "  \"total_skills_generated\": 50,\n" +
                      "  \"added_skills\": [\n" +
                      "    {\"name\": \"Rust\", \"category\": \"Programming Language\", \"description\": \"A fast, safe systems programming language.\"}\n"
                      +
                      "  ]\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> generateAndSaveTechSkills() {
    try {
      log.info("Starting tech skills generation");
      Map<String, Object> result = aiJobService.generateAndSaveTechSkills();
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error generating and saving tech skills", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Error generating and saving tech skills: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }
} 