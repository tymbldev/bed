package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.SkillRepository;
import com.tymbl.jobs.dto.JobRequest;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public JobResponse createJob(JobRequest request, User postedBy) {
        if (postedBy == null) {
            throw new RuntimeException("User must be authenticated to create a job");
        }
        if (postedBy.getId() == null) {
            throw new RuntimeException("Invalid user ID");
        }

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCityId(request.getCityId());
        job.setCountryId(request.getCountryId());
        job.setDesignationId(request.getDesignationId());
        job.setDesignation(request.getDesignation());
        job.setSalary(request.getSalary());
        job.setCurrencyId(request.getCurrencyId());
        job.setCompanyId(request.getCompanyId());
        job.setCompany(request.getCompany());
        job.setPostedById(postedBy.getId());
        
        // Set tags if provided
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            job.setTags(request.getTags());
        }

        job = jobRepository.save(job);
        return mapToResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getAllActiveJobs(Pageable pageable) {
        return jobRepository.findByActiveTrue(pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByUser(User user, Pageable pageable) {
        return jobRepository.findByPostedByIdAndActiveTrue(user.getId(), pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));
        return mapToResponse(job);
    }

    @Transactional
    public void deleteJob(Long jobId, User currentUser) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getPostedById().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this job");
        }

        job.setActive(false);
        jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, Pageable pageable) {
        return jobRepository.searchJobs(keyword, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchBySkills(List<String> skills, Pageable pageable) {
        if (skills == null || skills.isEmpty()) {
            return getAllActiveJobs(pageable);
        }
        
        // Convert skill names to skill IDs
        Set<Long> skillIds = skills.stream()
            .map(skillName -> skillRepository.findByNameContainingIgnoreCase(skillName))
            .filter(skillsList -> !skillsList.isEmpty())
            .map(skillsList -> skillsList.get(0).getId())
            .collect(Collectors.toSet());
        
        if (skillIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        return jobRepository.findBySkillsIn(skillIds, pageable)
            .map(this::mapToResponse);
    }

    public List<JobResponse> getJobsByCompany(Long companyId) {
        return jobRepository.findByCompanyId(companyId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCompanyAndTitle(Long companyId, String title) {
        return jobRepository.findActiveJobsByCompanyIdAndTitle(companyId, title).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCompanyName(String companyName) {
        return jobRepository.findByCompanyContainingIgnoreCase(companyName).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCompanyNameAndTitle(String companyName, String title) {
        return jobRepository.findActiveJobsByCompanyAndTitle(companyName, title).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, User currentUser) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));

        // Check if user is authorized to update the job
        if (!job.getPostedById().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this job");
        }

        // Update job details
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCityId(request.getCityId());
        job.setCountryId(request.getCountryId());
        job.setDesignationId(request.getDesignationId());
        job.setDesignation(request.getDesignation());
        job.setSalary(request.getSalary());
        job.setCurrencyId(request.getCurrencyId());
        job.setCompanyId(request.getCompanyId());
        job.setCompany(request.getCompany());
        
        // Update tags
        if (request.getTags() != null) {
            job.setTags(request.getTags());
        }

        job = jobRepository.save(job);
        return mapToResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobsByTags(Set<String> tags, Pageable pageable) {
        if (tags == null || tags.isEmpty()) {
            return getAllActiveJobs(pageable);
        }
        
        return jobRepository.findByTagsIn(tags, pageable)
            .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobsByTagKeyword(String tagKeyword, Pageable pageable) {
        if (tagKeyword == null || tagKeyword.isEmpty()) {
            return getAllActiveJobs(pageable);
        }
        
        return jobRepository.findByTagsContaining(tagKeyword, pageable)
            .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        return jobRepository.findAllTags();
    }

    public List<JobResponse> getJobsByCompanyPostedBySuperAdmin(Long companyId) {
        Long superAdminId = 0L; // Convention for super admin
        return jobRepository.findByCompanyIdAndPostedByIdAndActiveTrue(companyId, superAdminId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private JobResponse mapToResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setCityId(job.getCityId());
        response.setCountryId(job.getCountryId());
        response.setDesignationId(job.getDesignationId());
        response.setDesignation(job.getDesignation());
        response.setSalary(job.getSalary());
        response.setCurrencyId(job.getCurrencyId());
        response.setCompanyId(job.getCompanyId());
        response.setCompany(job.getCompany());
        response.setPostedBy(job.getPostedById());
        response.setActive(job.isActive());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        
        // Map tags to response
        if (job.getTags() != null) {
            response.setTags(job.getTags());
        }
        
        return response;
    }
}