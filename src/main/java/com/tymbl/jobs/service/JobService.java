package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobPostRequest;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public JobResponse createJob(JobPostRequest request, User postedBy) {
        Job job = new Job();
        mapRequestToJob(request, job);
        job.setPostedBy(postedBy);
        
        job = jobRepository.save(job);
        return mapJobToResponse(job);
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobPostRequest request, User currentUser) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this job");
        }

        mapRequestToJob(request, job);
        job = jobRepository.save(job);
        return mapJobToResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getAllActiveJobs(Pageable pageable) {
        return jobRepository.findByIsActiveTrue(pageable)
                .map(this::mapJobToResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByUser(User user, Pageable pageable) {
        return jobRepository.findByPostedByAndIsActiveTrue(user, pageable)
                .map(this::mapJobToResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return mapJobToResponse(job);
    }

    @Transactional
    public void deleteJob(Long jobId, User currentUser) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this job");
        }

        job.setActive(false);
        jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, Pageable pageable) {
        return jobRepository.searchJobs(keyword, pageable)
                .map(this::mapJobToResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchBySkills(List<String> skills, Pageable pageable) {
        return jobRepository.findBySkills(skills.stream().map(String::toLowerCase).collect(Collectors.toList()), pageable)
                .map(this::mapJobToResponse);
    }

    private void mapRequestToJob(JobPostRequest request, Job job) {
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(request.getCompany());
        job.setDepartment(request.getDepartment());
        job.setLocation(request.getLocation());
        job.setJobType(request.getJobType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setMinExperience(request.getMinExperience());
        job.setMaxExperience(request.getMaxExperience());
        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMaxSalary());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setQualifications(request.getQualifications());
        job.setResponsibilities(request.getResponsibilities());
        job.setEducationRequirement(request.getEducationRequirement());
        job.setWorkplaceType(request.getWorkplaceType());
        job.setRemoteAllowed(request.isRemoteAllowed());
        job.setApplicationDeadline(request.getApplicationDeadline());
        job.setNumberOfOpenings(request.getNumberOfOpenings());
        job.setNoticePeriod(request.getNoticePeriod());
        job.setReferralBonus(request.getReferralBonus());
    }

    private JobResponse mapJobToResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .company(job.getCompany())
                .department(job.getDepartment())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .experienceLevel(job.getExperienceLevel())
                .minExperience(job.getMinExperience())
                .maxExperience(job.getMaxExperience())
                .minSalary(job.getMinSalary())
                .maxSalary(job.getMaxSalary())
                .requiredSkills(job.getRequiredSkills())
                .qualifications(job.getQualifications())
                .responsibilities(job.getResponsibilities())
                .educationRequirement(job.getEducationRequirement())
                .workplaceType(job.getWorkplaceType())
                .isRemoteAllowed(job.isRemoteAllowed())
                .applicationDeadline(job.getApplicationDeadline())
                .numberOfOpenings(job.getNumberOfOpenings())
                .noticePeriod(job.getNoticePeriod())
                .referralBonus(job.getReferralBonus())
                .postedByUserName(job.getPostedBy().getFirstName() + " " + job.getPostedBy().getLastName())
                .postedDate(job.getPostedDate())
                .lastModifiedDate(job.getLastModifiedDate())
                .isActive(job.isActive())
                .build();
    }
} 