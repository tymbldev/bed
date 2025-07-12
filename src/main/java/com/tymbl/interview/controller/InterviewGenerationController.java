package com.tymbl.interview.controller;

import com.tymbl.interview.dto.InterviewTopicDTO;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/interview-generation")
@RequiredArgsConstructor
@Tag(name = "Interview Generation", description = "APIs for AI-powered interview content generation")
public class InterviewGenerationController {

    private final InterviewPreparationService interviewPreparationService;

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
        response.put("topics", topics);
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
        
        List<InterviewTopicDTO> savedTopics = interviewPreparationService.saveGeneratedTopics(designation, generatedTopics);
        
        Map<String, Object> response = new HashMap<>();
        response.put("designation", designation);
        response.put("generated_topics_count", generatedTopics.size());
        response.put("saved_topics_count", savedTopics.size());
        response.put("saved_topics", savedTopics);
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
        response.put("topics_by_designation", allTopics);
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
            
            if (!topics.isEmpty()) {
                List<InterviewTopicDTO> savedTopics = interviewPreparationService.saveGeneratedTopics(designation, topics);
                Map<String, Object> designationResult = new HashMap<>();
                designationResult.put("generated_count", topics.size());
                designationResult.put("saved_count", savedTopics.size());
                designationResult.put("saved_topics", savedTopics);
                results.put(designation, designationResult);
                totalSaved += savedTopics.size();
            } else {
                Map<String, Object> designationResult = new HashMap<>();
                designationResult.put("generated_count", 0);
                designationResult.put("saved_count", 0);
                designationResult.put("error", "No topics generated");
                results.put(designation, designationResult);
            }
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
} 