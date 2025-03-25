package com.tymbl.jobs.controller;

import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobPostRequest;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job posting and management APIs")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @Operation(summary = "Create a new job posting")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobPostRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(jobService.createJob(request, currentUser));
    }

    @PutMapping("/{jobId}")
    @Operation(summary = "Update an existing job posting")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobPostRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(jobService.updateJob(jobId, request, currentUser));
    }

    @GetMapping
    @Operation(summary = "Get all active job postings with pagination")
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        PageRequest pageRequest = PageRequest.of(
                page, size,
                Sort.Direction.valueOf(sortDirection),
                sortBy);
        return ResponseEntity.ok(jobService.getAllActiveJobs(pageRequest));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get job posting details by ID")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping("/my-posts")
    @Operation(summary = "Get all job postings by the current user")
    public ResponseEntity<Page<JobResponse>> getMyJobs(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                jobService.getJobsByUser(currentUser, PageRequest.of(page, size)));
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Delete (deactivate) a job posting")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User currentUser) {
        jobService.deleteJob(jobId, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search job postings by keyword")
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                jobService.searchJobs(keyword, PageRequest.of(page, size)));
    }

    @GetMapping("/search/skills")
    @Operation(summary = "Search job postings by required skills")
    public ResponseEntity<Page<JobResponse>> searchBySkills(
            @RequestParam List<String> skills,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                jobService.searchBySkills(skills, PageRequest.of(page, size)));
    }
} 