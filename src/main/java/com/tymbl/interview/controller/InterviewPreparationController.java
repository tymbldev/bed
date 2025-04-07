package com.tymbl.interview.controller;

import com.tymbl.interview.dto.CompanyDTO;
import com.tymbl.interview.dto.CompanyDesignationDTO;
import com.tymbl.interview.dto.InterviewQuestionDTO;
import com.tymbl.interview.dto.InterviewTopicDTO;
import com.tymbl.interview.service.InterviewPreparationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Tag(name = "Interview Preparation", description = "Endpoints for interview preparation content")
public class InterviewPreparationController {

    private final InterviewPreparationService interviewPreparationService;

    @GetMapping("/companies")
    @Operation(summary = "Get all companies with interview content")
    public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
        return ResponseEntity.ok(interviewPreparationService.getAllCompanies());
    }

    @GetMapping("/companies/{companyId}")
    @Operation(summary = "Get company details with available designations")
    public ResponseEntity<CompanyDTO> getCompanyWithDesignations(@PathVariable Long companyId) {
        return ResponseEntity.ok(interviewPreparationService.getCompanyWithDesignations(companyId));
    }

    @GetMapping("/companies/{companyId}/designations")
    @Operation(summary = "Get all designations for a company")
    public ResponseEntity<List<CompanyDesignationDTO>> getCompanyDesignations(@PathVariable Long companyId) {
        return ResponseEntity.ok(interviewPreparationService.getDesignationsByCompany(companyId));
    }

    @GetMapping("/companies/{companyId}/designations/{designationId}/skills")
    @Operation(summary = "Get all skills for a company-designation combination")
    public ResponseEntity<List<String>> getCompanyDesignationSkills(
            @PathVariable Long companyId,
            @PathVariable Long designationId) {
        return ResponseEntity.ok(interviewPreparationService.getSkillsByCompanyAndDesignation(companyId, designationId));
    }

    @GetMapping("/companies/{companyId}/designations/{designationId}/skills/{skillId}/topics")
    @Operation(summary = "Get all topics for a company-designation-skill combination")
    public ResponseEntity<List<InterviewTopicDTO>> getTopics(
            @PathVariable Long companyId,
            @PathVariable Long designationId,
            @PathVariable Long skillId) {
        return ResponseEntity.ok(interviewPreparationService.getTopicsByCompanyDesignationSkill(
                companyId, designationId, skillId));
    }

    @GetMapping("/topics/{topicId}")
    @Operation(summary = "Get topic details with questions")
    public ResponseEntity<InterviewTopicDTO> getTopicWithQuestions(@PathVariable Long topicId) {
        return ResponseEntity.ok(interviewPreparationService.getTopicWithQuestions(topicId));
    }

    @GetMapping("/questions/{questionId}")
    @Operation(summary = "Get interview question details")
    public ResponseEntity<InterviewQuestionDTO> getInterviewQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(interviewPreparationService.getInterviewQuestion(questionId));
    }
} 