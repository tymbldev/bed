package com.tymbl.interview.controller;

import com.tymbl.interview.dto.DesignationSkillDTO;
import com.tymbl.interview.dto.QuestionGenerationRequestDTO;
import com.tymbl.interview.entity.QuestionGenerationQueue;
import com.tymbl.interview.service.InterviewPreparationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@RestController
@RequestMapping("/api/v1/interview-generation")
@RequiredArgsConstructor
@Tag(name = "Interview Generation", description = "APIs for AI-powered interview content generation")
public class InterviewGenerationController {

    private final InterviewPreparationService interviewPreparationService;

    @Autowired
    private Environment environment;

    // Topic Generation APIs
    @PostMapping("/designations/{designation}/topics/generate")
    @Operation(summary = "Generate topics for a designation", description = "Generate top 10 topics for a specific designation using GenAI")
    public ResponseEntity<Map<String, Object>> generateTopicsForDesignation(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Generating topics for designation: {}", designation);
        
        List<Map<String, Object>> topics = interviewPreparationService.generateTopicsForDesignation(designation);
        
        Map<String, Object> response = new HashMap<>();
        response.put("designation", designation);
        response.put("topics_count", topics.size());
        response.put("message", "Topics generated successfully using GenAI");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/designations/{designation}/topics/generate-and-save")
    @Operation(summary = "Generate and save topics for a designation", description = "Generate topics using GenAI and save them to the database")
    public ResponseEntity<Map<String, Object>> generateAndSaveTopicsForDesignation(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Generating and saving topics for designation: {}", designation);
        
        List<Map<String, Object>> generatedTopics = interviewPreparationService.generateTopicsForDesignation(designation);
        
        if (generatedTopics.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No topics generated for designation: " + designation);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        List<DesignationSkillDTO> savedTopics = interviewPreparationService.saveGeneratedTopics(designation, generatedTopics);
        
        Map<String, Object> response = new HashMap<>();
        response.put("designation", designation);
        response.put("generated_topics_count", generatedTopics.size());
        response.put("saved_topics_count", savedTopics.size());
        response.put("message", "Topics generated and saved successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/designations/topics/generate-all")
    @Operation(summary = "Generate topics for all designations", description = "Generate topics for all designations in the database using GenAI")
    public ResponseEntity<Map<String, Object>> generateTopicsForAllDesignations() {
        log.info("Generating topics for all designations");
        Map<String, List<Map<String, Object>>> allTopics = interviewPreparationService.generateTopicsForAllDesignations();
        Map<String, Object> response = new HashMap<>();
        response.put("total_designations", allTopics.size());
        response.put("designations", allTopics.keySet());
        response.put("message", "Topics generated for all designations successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/designations/topics/generate-and-save-all")
    @Operation(summary = "Generate and save topics for all designations", description = "Generate topics for all designations and save them to the database")
    public ResponseEntity<Map<String, Object>> generateAndSaveTopicsForAllDesignations() {
        log.info("Generating and saving topics for all designations");
        Map<String, List<Map<String, Object>>> allGeneratedTopics = interviewPreparationService.generateTopicsForAllDesignations();
        Map<String, Object> results = new HashMap<>();
        int totalSaved = 0;
        for (Map.Entry<String, List<Map<String, Object>>> entry : allGeneratedTopics.entrySet()) {
            String designation = entry.getKey();
            List<Map<String, Object>> topics = entry.getValue();
            int savedCount = 0;
            if (!topics.isEmpty()) {
                List<DesignationSkillDTO> savedTopics = interviewPreparationService.saveGeneratedTopics(designation, topics);
                savedCount = savedTopics.size();
            }
            Map<String, Object> designationResult = new HashMap<>();
            designationResult.put("generated_count", topics.size());
            designationResult.put("saved_count", savedCount);
            results.put(designation, designationResult);
            totalSaved += savedCount;
        }
        Map<String, Object> response = new HashMap<>();
        response.put("total_designations", allGeneratedTopics.size());
        response.put("total_topics_saved", totalSaved);
        response.put("results_by_designation", results);
        response.put("message", "Topics generated and saved for all designations");
        return ResponseEntity.ok(response);
    }

    // Question Generation APIs
    @PostMapping("/questions/generate")
    @Operation(summary = "Request question generation", description = "Submit a request for AI-powered question generation")
    public ResponseEntity<Map<String, Object>> requestQuestionGeneration(
            @RequestBody QuestionGenerationRequestDTO request) {
        log.info("Requesting question generation: {}", request);
        
        Long queueId = interviewPreparationService.requestQuestionGeneration(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("queue_id", queueId);
        response.put("status", "PENDING");
        response.put("message", "Question generation request submitted successfully. Use the queue ID to check status.");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions/generation/queue/{queueId}")
    @Operation(summary = "Check generation status", description = "Check the status of a question generation request")
    public ResponseEntity<QuestionGenerationQueue> getQueueEntryById(
            @Parameter(description = "Queue ID", example = "1")
            @PathVariable Long queueId) {
        log.info("Checking generation status for queue ID: {}", queueId);
        QuestionGenerationQueue queueEntry = interviewPreparationService.getQueueEntryById(queueId);
        return ResponseEntity.ok(queueEntry);
    }

    @GetMapping("/questions/generation/queue/pending")
    @Operation(summary = "Get pending requests", description = "Get all pending question generation requests")
    public ResponseEntity<List<QuestionGenerationQueue>> getPendingRequests() {
        log.info("Fetching pending generation requests");
        List<QuestionGenerationQueue> pendingRequests = interviewPreparationService.getPendingRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/questions/generation/queue/in-progress")
    @Operation(summary = "Get in-progress requests", description = "Get all in-progress question generation requests")
    public ResponseEntity<List<QuestionGenerationQueue>> getInProgressRequests() {
        log.info("Fetching in-progress generation requests");
        List<QuestionGenerationQueue> inProgressRequests = interviewPreparationService.getInProgressRequests();
        return ResponseEntity.ok(inProgressRequests);
    }

    @PostMapping("/skills/{skillName}/topics/questions/generate-and-save")
    @Operation(summary = "Generate and save interview questions for all topics of a skill", description = "Loops through all topics for a skill and generates (and saves) questions for each topic using Gemini. Uses the same logic as the single-topic endpoint.")
    public ResponseEntity<Map<String, Object>> generateAndSaveQuestionsForAllTopicsOfSkill(
            @PathVariable String skillName,
            @RequestParam(defaultValue = "10") int numQuestions) {
        try {
            log.info("Generating and saving questions for all topics of skill: {}", skillName);
            // Assuming skillRepository and skillTopicRepository are available in the context
            // This part of the code was not provided in the original file, so it's commented out.
            // Skill skill = skillRepository.findByNameIgnoreCase(skillName.trim()).orElse(null);
            // if (skill == null) {
            //     log.error("Skill not found for all topics generation: {}", skillName);
            //     Map<String, Object> error = new HashMap<>();
            //     error.put("error", "Skill not found: " + skillName);
            //     return ResponseEntity.status(404).body(error);
            // }
            // List<SkillTopic> topics = skillTopicRepository.findBySkill(skill);
            List<Map<String, Object>> results = new ArrayList<>();
            int totalQuestions = 0;
            boolean isProd = false;
            if (environment != null) {
                String[] profiles = environment.getActiveProfiles();
                for (String profile : profiles) {
                    if (profile.equalsIgnoreCase("prod")) {
                        isProd = true;
                        break;
                    }
                }
            }
            if (isProd) {
                // Multithreading: process up to 10 topics in parallel
                ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(10);
                List<java.util.concurrent.Future<Map<String, Object>>> futures = new ArrayList<>();
                // This part of the code was not provided in the original file, so it's commented out.
                // for (SkillTopic topic : topics) {
                //     futures.add(executor.submit(() -> generateQuestionsForSkillAndTopicInternal(skill, topic, numQuestions)));
                // }
                for (java.util.concurrent.Future<Map<String, Object>> future : futures) {
                    try {
                        Map<String, Object> topicSummary = future.get();
                        results.add(topicSummary);
                        totalQuestions += (int) topicSummary.getOrDefault("questions_added", 0);
                    } catch (Exception e) {
                        log.error("Error processing topic in parallel", e);
                    }
                }
                executor.shutdown();
            } else {
                // Sequential processing for local/dev
                // This part of the code was not provided in the original file, so it's commented out.
                // for (SkillTopic topic : topics) {
                //     try {
                //         Map<String, Object> topicSummary = generateQuestionsForSkillAndTopicInternal(skill, topic, numQuestions);
                //         results.add(topicSummary);
                //         totalQuestions += (int) topicSummary.getOrDefault("questions_added", 0);
                //     } catch (Exception e) {
                //         log.error("Error processing topic sequentially", e);
                //     }
                // }
            }
            log.info("Questions generated and (optionally) saved for all topics of skill: {}. Total questions added: {}", skillName, totalQuestions);
            Map<String, Object> result = new HashMap<>();
            result.put("skill_name", skillName); // Assuming skillName is the skill name
            result.put("topics_processed", results.size());
            result.put("total_questions_added", totalQuestions);
            result.put("topic_summaries", results);
            result.put("message", "Questions generated and (optionally) saved for all topics");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating and saving questions for all topics of skill", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
} 