package com.tymbl.interview.controller;

import com.tymbl.interview.dto.CompanyDTO;
import com.tymbl.interview.dto.CompanyDesignationDTO;
import com.tymbl.interview.dto.InterviewQuestionDTO;
import com.tymbl.interview.dto.InterviewTopicDTO;
import com.tymbl.interview.service.InterviewPreparationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(
    origins = "*", 
    allowedHeaders = "*", 
    methods = {
        RequestMethod.GET, 
        RequestMethod.POST, 
        RequestMethod.PUT, 
        RequestMethod.DELETE, 
        RequestMethod.OPTIONS, 
        RequestMethod.PATCH
    }
)
@RequestMapping("/api/v1/interview-preparation")
@RequiredArgsConstructor
@Tag(name = "Interview Preparation", description = "Endpoints for interview preparation content")
public class InterviewPreparationController {

    private final InterviewPreparationService interviewPreparationService;

    @GetMapping("/companies")
    @Operation(
        summary = "Get all companies",
        description = "Returns a list of all companies with their interview preparation content."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of companies retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyDTO.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"Google\",\n" +
                          "    \"description\": \"Technology company\",\n" +
                          "    \"interviewProcess\": \"Multiple rounds including coding, system design, and behavioral interviews\",\n" +
                          "    \"preparationTips\": \"Focus on data structures, algorithms, and system design\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"Amazon\",\n" +
                          "    \"description\": \"E-commerce and cloud computing company\",\n" +
                          "    \"interviewProcess\": \"Online assessment followed by onsite interviews\",\n" +
                          "    \"preparationTips\": \"Practice leadership principles and coding problems\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
        return ResponseEntity.ok(interviewPreparationService.getAllCompanies());
    }

    @GetMapping("/companies/{companyId}")
    @Operation(
        summary = "Get company details with available designations",
        description = "Returns detailed information about a specific company including available designations for interview preparation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Company details retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<CompanyDTO> getCompanyWithDesignations(@PathVariable Long companyId) {
        return ResponseEntity.ok(interviewPreparationService.getCompanyWithDesignations(companyId));
    }

    @GetMapping("/companies/{companyId}/designations")
    @Operation(
        summary = "Get designations for a company",
        description = "Returns a list of designations and their interview preparation content for a specific company."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of designations retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyDesignationDTO.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"Software Engineer\",\n" +
                          "    \"description\": \"Entry-level software development position\",\n" +
                          "    \"interviewProcess\": \"Coding rounds followed by system design\",\n" +
                          "    \"preparationTips\": \"Focus on data structures and algorithms\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"Senior Software Engineer\",\n" +
                          "    \"description\": \"Experienced software development position\",\n" +
                          "    \"interviewProcess\": \"System design and leadership rounds\",\n" +
                          "    \"preparationTips\": \"Focus on system design and leadership principles\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<List<CompanyDesignationDTO>> getDesignationsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(interviewPreparationService.getDesignationsByCompany(companyId));
    }

    @GetMapping("/companies/{companyId}/designations/{designationId}/skills")
    @Operation(
        summary = "Get all skills for a company-designation combination",
        description = "Returns a list of skills required for a specific designation at a particular company."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Skills retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Company or designation not found")
    })
    public ResponseEntity<List<String>> getCompanyDesignationSkills(
            @PathVariable Long companyId,
            @PathVariable Long designationId) {
        return ResponseEntity.ok(interviewPreparationService.getSkillsByCompanyAndDesignation(companyId, designationId));
    }

    @GetMapping("/companies/{companyId}/designations/{designationId}/skills/{skillId}/topics")
    @Operation(
        summary = "Get all topics for a company-designation-skill combination",
        description = "Returns interview preparation topics for a specific skill at a particular designation and company."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topics retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = InterviewTopicDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Company, designation, or skill not found")
    })
    public ResponseEntity<List<InterviewTopicDTO>> getTopics(
            @PathVariable Long companyId,
            @PathVariable Long designationId,
            @PathVariable Long skillId) {
        return ResponseEntity.ok(interviewPreparationService.getTopicsByCompanyDesignationSkill(
                companyId, designationId, skillId));
    }

    @GetMapping("/topics")
    @Operation(
        summary = "Get all interview topics",
        description = "Returns a list of all interview preparation topics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of topics retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = InterviewTopicDTO.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"Data Structures\",\n" +
                          "    \"description\": \"Fundamental data structures and their implementations\",\n" +
                          "    \"difficultyLevel\": \"INTERMEDIATE\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"System Design\",\n" +
                          "    \"description\": \"Designing scalable and distributed systems\",\n" +
                          "    \"difficultyLevel\": \"ADVANCED\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<InterviewTopicDTO>> getAllTopics() {
        return ResponseEntity.ok(interviewPreparationService.getAllTopics());
    }

    @GetMapping("/topics/{topicId}")
    @Operation(
        summary = "Get topic details with questions",
        description = "Returns detailed information about a specific interview topic including all related questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topic details retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = InterviewTopicDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    public ResponseEntity<InterviewTopicDTO> getTopicWithQuestions(@PathVariable Long topicId) {
        return ResponseEntity.ok(interviewPreparationService.getTopicWithQuestions(topicId));
    }

    @GetMapping("/questions")
    @Operation(
        summary = "Get interview questions by topic",
        description = "Returns a list of interview questions for a specific topic."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of questions retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = InterviewQuestionDTO.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"question\": \"What is the difference between ArrayList and LinkedList?\",\n" +
                          "    \"answer\": \"ArrayList is a resizable array implementation...\",\n" +
                          "    \"difficultyLevel\": \"INTERMEDIATE\",\n" +
                          "    \"topicId\": 1\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"question\": \"How would you design a URL shortening service?\",\n" +
                          "    \"answer\": \"A URL shortening service requires...\",\n" +
                          "    \"difficultyLevel\": \"ADVANCED\",\n" +
                          "    \"topicId\": 2\n" +
                          "  }\n" +
                          "]"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    public ResponseEntity<List<InterviewQuestionDTO>> getQuestionsByTopic(@RequestParam Long topicId) {
        return ResponseEntity.ok(interviewPreparationService.getQuestionsByTopic(topicId));
    }

    @GetMapping("/questions/{questionId}")
    @Operation(
        summary = "Get interview question details",
        description = "Returns detailed information about a specific interview question."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Question details retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = InterviewQuestionDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Question not found")
    })
    public ResponseEntity<InterviewQuestionDTO> getInterviewQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(interviewPreparationService.getInterviewQuestion(questionId));
    }
} 