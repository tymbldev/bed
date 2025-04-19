package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public JobApplicationResponse applyForJob(JobApplicationRequest request, User applicant) {
        Job job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));

        // Check if already applied
        List<JobApplication> existingApplications = jobApplicationRepository.findByJobIdAndApplicantId(job.getId(), applicant.getId());
        if (existingApplications != null && !existingApplications.isEmpty()) {
            throw new RuntimeException("You have already applied for this job");
        }

        JobApplication application = new JobApplication();
        application.setJobId(job.getId());
        application.setApplicantId(applicant.getId());
        application.setCoverLetter(request.getCoverLetter());
        application.setResumeUrl(request.getResumeUrl());
        application.setStatus(JobApplication.ApplicationStatus.PENDING);

        application = jobApplicationRepository.save(application);
        return mapToBasicResponse(application);
    }

    public List<JobApplicationResponse> getApplicationsByJob(Long jobId) {
        return jobApplicationRepository.findByJobId(jobId).stream()
            .map(this::mapToBasicResponse)
            .collect(Collectors.toList());
    }

    public List<JobApplicationResponse> getApplicationsByApplicant(User applicant) {
        return jobApplicationRepository.findByApplicantId(applicant.getId()).stream()
            .map(this::mapToBasicResponse)
            .collect(Collectors.toList());
    }

    public JobApplicationResponseExtendedDetails getApplicationDetails(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        return mapToExtendedDetails(application);
    }

    private JobApplicationResponse mapToBasicResponse(JobApplication application) {
        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));
        User applicant = userRepository.findById(application.getApplicantId())
            .orElseThrow(() -> new RuntimeException("Applicant not found"));

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(application.getId());
        response.setJobId(job.getId());
        response.setJobTitle(job.getTitle());
        response.setApplicantId(applicant.getId());
        response.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
        response.setStatus(convertStatus(application.getStatus()));
        response.setCreatedAt(application.getCreatedAt());
        return response;
    }

    private JobApplicationResponseExtendedDetails mapToExtendedDetails(JobApplication application) {
        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));
        User applicant = userRepository.findById(application.getApplicantId())
            .orElseThrow(() -> new RuntimeException("Applicant not found"));

        JobApplicationResponseExtendedDetails details = new JobApplicationResponseExtendedDetails();
        details.setId(application.getId());
        details.setJobId(job.getId());
        details.setJobTitle(job.getTitle());
        details.setJobDescription(job.getDescription());
        details.setJobCityId(job.getCityId());
        details.setJobCountryId(job.getCountryId());
        details.setJobDesignationId(job.getDesignationId());
        details.setJobDesignation(job.getDesignation());
        details.setJobSalary(job.getSalary());
        details.setJobCurrencyId(job.getCurrencyId());
        details.setJobCompanyId(job.getCompanyId());
        details.setJobCompany(job.getCompany());
        details.setJobSkillIds(new ArrayList<>(job.getSkillIds()));
        details.setApplicantId(applicant.getId());
        details.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
        details.setApplicantEmail(applicant.getEmail());
        details.setCoverLetter(application.getCoverLetter());
        details.setResumeUrl(application.getResumeUrl());
        details.setStatus(convertStatus(application.getStatus()));
        details.setApplicantSkillIds(new ArrayList<>(applicant.getSkillIds()));
        details.setExperience(applicant.getYearsOfExperience() + " years " + applicant.getMonthsOfExperience() + " months");
        details.setEducation(applicant.getEducation().stream()
            .map(edu -> edu.getDegree() + " from " + edu.getInstitution())
            .collect(Collectors.joining(", ")));
        details.setPortfolioUrl(applicant.getPortfolioWebsite());
        details.setLinkedInUrl(applicant.getLinkedInProfile());
        details.setGithubUrl(applicant.getGithubProfile());
        details.setCreatedAt(application.getCreatedAt());
        details.setUpdatedAt(application.getUpdatedAt());
        return details;
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

    @Transactional(readOnly = true)
    public List<JobApplicationResponseExtendedDetails> getJobApplicationsByUser(User user) {
        return jobApplicationRepository.findByApplicantId(user.getId()).stream()
            .map(this::mapToExtendedDetails)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponseExtendedDetails> getJobApplicationsByJob(Long jobId) {
        return jobApplicationRepository.findByJobId(jobId).stream()
            .map(this::mapToExtendedDetails)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobApplicationResponseExtendedDetails getJobApplication(Long applicationId) {
        return jobApplicationRepository.findById(applicationId)
            .map(this::mapToExtendedDetails)
            .orElseThrow(() -> new RuntimeException("Job application not found"));
    }
} 