package com.tymbl.interview.controller;

import com.tymbl.interview.dto.DesignationSkillDTO;
import com.tymbl.interview.dto.InterviewQuestionDTO;
import com.tymbl.interview.service.InterviewPreparationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/interview-prep")
@RequiredArgsConstructor
@Tag(name = "Interview Preparation", description = "APIs for interview preparation content management")
public class InterviewPreparationController {

    private final InterviewPreparationService interviewPreparationService;

    // Topic Management APIs
    @GetMapping("/designations")
    @Operation(summary = "Get all designations", description = "Retrieve all available designations")
    public ResponseEntity<List<String>> getAllDesignations() {
        log.info("Fetching all available designations");
        List<String> designations = interviewPreparationService.getAllDesignations();
        return ResponseEntity.ok(designations);
    }

    @GetMapping("/designations/database")
    @Operation(summary = "Get all designations from database", description = "Retrieve all designations from the designation table")
    public ResponseEntity<List<String>> getAllDesignationsFromDatabase() {
        log.info("Fetching all designations from database");
        List<String> designations = interviewPreparationService.getAllDesignationsFromDatabase();
        return ResponseEntity.ok(designations);
    }

    @GetMapping("/designations/{designation}/topics")
    @Operation(summary = "Get topics by designation", description = "Retrieve all topics for a specific designation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Designation not found")
    })
    public ResponseEntity<List<DesignationSkillDTO>> getTopicsByDesignation(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Fetching topics for designation: {}", designation);
        List<DesignationSkillDTO> topics = interviewPreparationService.getTopicsByDesignation(designation);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/designations/{designation}/topics/filter")
    @Operation(summary = "Get topics by designation with filters", description = "Retrieve topics for a designation with optional difficulty and category filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Designation not found")
    })
    public ResponseEntity<List<DesignationSkillDTO>> getTopicsByDesignationWithFilters(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation,
            @Parameter(description = "Difficulty level", example = "INTERMEDIATE")
            @RequestParam(required = false) String difficultyLevel,
            @Parameter(description = "Category", example = "Data Structures")
            @RequestParam(required = false) String category) {
        log.info("Fetching topics for designation: {} with filters - difficulty: {}, category: {}", 
                designation, difficultyLevel, category);
        
        List<DesignationSkillDTO> topics;
        if (difficultyLevel != null && category != null) {
            // For now, just filter by difficulty since we don't have a combined method
            topics = interviewPreparationService.getTopicsByDesignationAndDifficulty(designation, 
                com.tymbl.interview.entity.DesignationSkill.DifficultyLevel.valueOf(difficultyLevel.toUpperCase()));
        } else if (difficultyLevel != null) {
            topics = interviewPreparationService.getTopicsByDesignationAndDifficulty(designation, 
                com.tymbl.interview.entity.DesignationSkill.DifficultyLevel.valueOf(difficultyLevel.toUpperCase()));
        } else if (category != null) {
            topics = interviewPreparationService.getTopicsByDesignationAndCategory(designation, category);
        } else {
            topics = interviewPreparationService.getTopicsByDesignation(designation);
        }
        
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/designations/{designation}/categories")
    @Operation(summary = "Get categories by designation", description = "Retrieve all categories available for a designation")
    public ResponseEntity<List<String>> getCategoriesByDesignation(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Fetching categories for designation: {}", designation);
        List<String> categories = interviewPreparationService.getCategoriesByDesignation(designation);
        return ResponseEntity.ok(categories);
    }

    // General Questions Management APIs
    @GetMapping("/designations/{designation}/questions/general")
    @Operation(summary = "Get general questions by designation", description = "Retrieve all general questions for a designation")
    public ResponseEntity<List<InterviewQuestionDTO>> getGeneralQuestionsByDesignation(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Fetching general questions for designation: {}", designation);
        List<InterviewQuestionDTO> questions = interviewPreparationService.getGeneralQuestionsByDesignation(designation);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/designations/{designation}/topics/{topicName}/questions/general")
    @Operation(summary = "Get general questions by designation and topic", description = "Retrieve general questions for a specific topic")
    public ResponseEntity<List<InterviewQuestionDTO>> getGeneralQuestionsByDesignationAndTopic(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation,
            @Parameter(description = "Topic name", example = "Data Structures & Algorithms")
            @PathVariable String topicName) {
        log.info("Fetching general questions for designation: {} and topic: {}", designation, topicName);
        List<InterviewQuestionDTO> questions = interviewPreparationService.getGeneralQuestionsByDesignationAndTopic(designation, topicName);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/designations/{designation}/topics/{topicName}/questions/general/paginated")
    @Operation(summary = "Get paginated general questions", description = "Retrieve paginated general questions for a topic")
    public ResponseEntity<Page<InterviewQuestionDTO>> getGeneralQuestionsByDesignationAndTopicPaginated(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation,
            @Parameter(description = "Topic name", example = "Data Structures & Algorithms")
            @PathVariable String topicName,
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching paginated general questions for designation: {} and topic: {}", designation, topicName);
        Page<InterviewQuestionDTO> questions = interviewPreparationService.getGeneralQuestionsByDesignationAndTopicPaginated(designation, topicName, page, size);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/designations/{designation}/topics-with-questions/general")
    @Operation(summary = "Get topics with general questions", description = "Retrieve topics that have general questions")
    public ResponseEntity<List<String>> getTopicsWithGeneralQuestions(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Fetching topics with general questions for designation: {}", designation);
        List<String> topics = interviewPreparationService.getTopicsWithGeneralQuestions(designation);
        return ResponseEntity.ok(topics);
    }

    // Company-Specific Questions Management APIs
    @GetMapping("/companies/{companyName}/designations/{designation}/questions")
    @Operation(summary = "Get company questions", description = "Retrieve company-specific questions")
    public ResponseEntity<List<InterviewQuestionDTO>> getCompanyQuestionsByCompanyAndDesignation(
            @Parameter(description = "Company name", example = "Google")
            @PathVariable String companyName,
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Fetching company questions for company: {} and designation: {}", companyName, designation);
        List<InterviewQuestionDTO> questions = interviewPreparationService.getCompanyQuestionsByCompanyAndDesignation(companyName, designation);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/companies/{companyName}/designations/{designation}/topics/{topicName}/questions")
    @Operation(summary = "Get company questions by topic", description = "Retrieve company-specific questions for a topic")
    public ResponseEntity<List<InterviewQuestionDTO>> getCompanyQuestionsByCompanyAndDesignationAndTopic(
            @Parameter(description = "Company name", example = "Google")
            @PathVariable String companyName,
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation,
            @Parameter(description = "Topic name", example = "System Design")
            @PathVariable String topicName) {
        log.info("Fetching company questions for company: {}, designation: {} and topic: {}", companyName, designation, topicName);
        List<InterviewQuestionDTO> questions = interviewPreparationService.getCompanyQuestionsByCompanyAndDesignationAndTopic(companyName, designation, topicName);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/companies/{companyName}/designations/{designation}/topics/{topicName}/questions/paginated")
    @Operation(summary = "Get paginated company questions", description = "Retrieve paginated company-specific questions")
    public ResponseEntity<Page<InterviewQuestionDTO>> getCompanyQuestionsByCompanyAndDesignationAndTopicPaginated(
            @Parameter(description = "Company name", example = "Google")
            @PathVariable String companyName,
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation,
            @Parameter(description = "Topic name", example = "System Design")
            @PathVariable String topicName,
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching paginated company questions for company: {}, designation: {} and topic: {}", companyName, designation, topicName);
        Page<InterviewQuestionDTO> questions = interviewPreparationService.getCompanyQuestionsByCompanyAndDesignationAndTopicPaginated(companyName, designation, topicName, page, size);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/designations/{designation}/companies-with-questions")
    @Operation(summary = "Get companies with questions", description = "Retrieve companies that have questions for a designation")
    public ResponseEntity<List<String>> getCompaniesWithQuestions(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Fetching companies with questions for designation: {}", designation);
        List<String> companies = interviewPreparationService.getCompaniesWithQuestions(designation);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/companies/{companyName}/designations/{designation}/topics-with-questions")
    @Operation(summary = "Get topics with company questions", description = "Retrieve topics that have company-specific questions")
    public ResponseEntity<List<String>> getTopicsWithCompanyQuestions(
            @Parameter(description = "Company name", example = "Google")
            @PathVariable String companyName,
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Fetching topics with company questions for company: {} and designation: {}", companyName, designation);
        List<String> topics = interviewPreparationService.getTopicsWithCompanyQuestions(companyName, designation);
        return ResponseEntity.ok(topics);
    }

    // Statistics APIs
    @GetMapping("/designations/{designation}/statistics")
    @Operation(summary = "Get interview preparation statistics", description = "Retrieve comprehensive statistics for interview preparation")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @Parameter(description = "Designation name", example = "Software Engineer")
            @PathVariable String designation) {
        log.info("Fetching statistics for designation: {}", designation);
        Map<String, Object> statistics = interviewPreparationService.getStatistics(designation);
        return ResponseEntity.ok(statistics);
    }
} 