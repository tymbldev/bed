package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobApplicationRequest;
import com.tymbl.jobs.dto.JobApplicationResponse;
import com.tymbl.jobs.dto.JobApplicationResponseExtendedDetails;
import com.tymbl.jobs.entity.ApplicationStatus;
import com.tymbl.jobs.entity.JobApplication;
import com.tymbl.jobs.repository.JobApplicationRepository;
import com.tymbl.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;

    @Transactional
    public JobApplicationResponse applyForJob(JobApplicationRequest request, User applicant) {
        Job job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));

        if (jobApplicationRepository.existsByJobAndApplicant(job, applicant)) {
            throw new RuntimeException("You have already applied for this job");
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setApplicant(applicant);
        application.setCoverLetter(request.getCoverLetter());
        application.setResumeUrl(request.getResumeUrl());

        application = jobApplicationRepository.save(application);
        return mapToBasicResponse(application);
    }

    public List<JobApplicationResponse> getApplicationsByJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));

        return jobApplicationRepository.findByJob(job).stream()
            .map(this::mapToBasicResponse)
            .collect(Collectors.toList());
    }

    public List<JobApplicationResponse> getApplicationsByApplicant(User applicant) {
        return jobApplicationRepository.findByApplicant(applicant).stream()
            .map(this::mapToBasicResponse)
            .collect(Collectors.toList());
    }

    public JobApplicationResponseExtendedDetails getApplicationDetails(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        return mapToExtendedResponse(application);
    }

    private JobApplicationResponse mapToBasicResponse(JobApplication application) {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(application.getId());
        response.setJobId(application.getJob().getId());
        response.setJobTitle(application.getJob().getTitle());
        response.setApplicantId(application.getApplicant().getId());
        response.setApplicantName(application.getApplicant().getFirstName() + " " + application.getApplicant().getLastName());
        response.setStatus(convertStatus(application.getStatus()));
        response.setCreatedAt(application.getAppliedAt());
        return response;
    }

    private JobApplicationResponseExtendedDetails mapToExtendedResponse(JobApplication application) {
        JobApplicationResponseExtendedDetails response = new JobApplicationResponseExtendedDetails();
        response.setId(application.getId());
        response.setJobId(application.getJob().getId());
        response.setJobTitle(application.getJob().getTitle());
        response.setJobDescription(application.getJob().getDescription());
        response.setJobLocation(application.getJob().getLocation());
        response.setJobEmploymentType(application.getJob().getEmploymentType());
        response.setJobExperienceLevel(application.getJob().getExperienceLevel());
        response.setJobSalary(application.getJob().getSalary());
        response.setJobCurrency(application.getJob().getCurrency());
        response.setApplicantId(application.getApplicant().getId());
        response.setApplicantName(application.getApplicant().getFirstName() + " " + application.getApplicant().getLastName());
        response.setApplicantEmail(application.getApplicant().getEmail());
        response.setCoverLetter(application.getCoverLetter());
        response.setResumeUrl(application.getResumeUrl());
        response.setStatus(convertStatus(application.getStatus()));
        
        // These fields may not exist in the entity, setting them to null or default values
        response.setSkills(new ArrayList<>());
        response.setExperience(null);
        response.setEducation(null);
        response.setPortfolioUrl(application.getApplicant().getPortfolioUrl());
        response.setLinkedInUrl(application.getApplicant().getLinkedInProfile());
        response.setGithubUrl(null);
        response.setCreatedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }
    
    /**
     * Convert from JobApplication.ApplicationStatus to ApplicationStatus
     */
    private ApplicationStatus convertStatus(JobApplication.ApplicationStatus status) {
        switch (status) {
            case PENDING:
                return ApplicationStatus.PENDING;
            case REVIEWING:
                return ApplicationStatus.REVIEWING;
            case SHORTLISTED:
                return ApplicationStatus.SHORTLISTED;
            case REJECTED:
                return ApplicationStatus.REJECTED;
            case HIRED:
                return ApplicationStatus.ACCEPTED;
            default:
                return ApplicationStatus.PENDING;
        }
    }
} 