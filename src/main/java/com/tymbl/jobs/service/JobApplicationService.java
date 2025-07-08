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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(JobApplicationService.class);

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;

    /**
     * Helper method to enrich user data with company name from dropdown
     */
    private User enrichUserWithCompanyName(User user) {
        try {
            if (user.getCompanyId() != null) {
                // Fetch company name from CompanyService using companyId
                String companyName = companyService.getCompanyById(user.getCompanyId()).getName();
                user.setCompany(companyName);
            }
        } catch (Exception e) {
            logger.warn("Could not fetch company name for companyId: {}. Error: {}", user.getCompanyId(), e.getMessage());
            // Keep the existing company field as is if there's an error
        }
        return user;
    }

    @Transactional
    public JobApplicationResponse applyForJob(JobApplicationRequest request, User applicant) {
        // Validate input parameters
        if (request == null) {
            throw new RuntimeException("Job application request cannot be null");
        }
        if (applicant == null) {
            throw new RuntimeException("Applicant cannot be null");
        }
        if (request.getJobId() == null) {
            throw new RuntimeException("Job ID is required");
        }
        if (request.getJobReferrerId() == null) {
            throw new RuntimeException("Job Referrer ID is required");
        }
        if (request.getCoverLetter() == null || request.getCoverLetter().trim().isEmpty()) {
            throw new RuntimeException("Cover letter is required");
        }

        Job job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));

        // Check if user is trying to apply to their own job
        if (job.getPostedById().equals(applicant.getId())) {
            throw new RuntimeException("You cannot apply to your own job posting");
        }

        // Check if already applied for this job (any referrer)
        List<JobApplication> existingApplications = jobApplicationRepository.findByJobIdAndApplicantId(job.getId(), applicant.getId());
        if (existingApplications != null && !existingApplications.isEmpty()) {
            throw new RuntimeException("You have already applied for this job");
        }

        JobApplication application = new JobApplication();
        application.setJobId(job.getId());
        application.setApplicantId(applicant.getId());
        application.setJobReferrerId(request.getJobReferrerId());
        application.setCoverLetter(request.getCoverLetter());
        application.setResumeUrl(applicant.getResume());
        application.setStatus(JobApplication.ApplicationStatus.PENDING);

        application = jobApplicationRepository.save(application);
        return mapToBasicResponse(application);
    }

    public List<JobApplicationResponse> getApplicationsByJob(Long jobId) {
        return jobApplicationRepository.findByJobId(jobId).stream()
            .map(this::mapToBasicResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getApplicationsByApplicant(User applicant) {
        return jobApplicationRepository.findByApplicantId(applicant.getId()).stream()
            .map(this::mapToBasicResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponseExtendedDetails> getApplicationsForJobsPostedByUser(User user) {
        List<Job> userJobs = jobRepository.findByPostedById(user.getId());
        List<Long> jobIds = userJobs.stream().map(Job::getId).collect(Collectors.toList());
        
        return jobApplicationRepository.findByJobIdIn(jobIds).stream()
            .map(this::mapToExtendedDetails)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponseExtendedDetails> getApplicationsByJob(Long jobId, User user) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));

        // Verify that the user is the job poster
        if (!job.getPostedById().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to view applications for this job");
        }

        return jobApplicationRepository.findByJobId(jobId).stream()
            .map(this::mapToExtendedDetails)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobApplicationResponseExtendedDetails getApplicationDetails(Long applicationId, User user) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));

        // Verify that the user is the job poster
        if (!job.getPostedById().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to view this application");
        }

        return mapToExtendedDetails(application);
    }

    private JobApplicationResponse mapToBasicResponse(JobApplication application) {
        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));
        User applicant = userRepository.findById(application.getApplicantId())
            .orElseThrow(() -> new RuntimeException("Applicant not found"));

        // Enrich applicant data with company name from dropdown
        applicant = enrichUserWithCompanyName(applicant);

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(application.getId());
        response.setJobId(job.getId());
        response.setJobTitle(job.getTitle());
        response.setApplicantId(applicant.getId());
        response.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
        response.setStatus(convertStatus(application.getStatus()));
        response.setCreatedAt(application.getCreatedAt());
        response.setJobReferrerId(application.getJobReferrerId());

        // Populate referrer sudo identity
        if (application.getJobReferrerId() != null) {
            userRepository.findById(application.getJobReferrerId()).ifPresent(refUser -> {
                // Enrich referrer data with company name from dropdown
                refUser = enrichUserWithCompanyName(refUser);
                
                com.tymbl.jobs.dto.SudoIdentityDTO sudo = new com.tymbl.jobs.dto.SudoIdentityDTO();
                sudo.setDesignation(refUser.getDesignation());
                sudo.setCompany(refUser.getCompany());
                response.setReferrerSudoIdentity(sudo);
            });
        }
        return response;
    }

    private JobApplicationResponseExtendedDetails mapToExtendedDetails(JobApplication application) {
        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));
        User applicant = userRepository.findById(application.getApplicantId())
            .orElseThrow(() -> new RuntimeException("Applicant not found"));

        // Enrich applicant data with company name from dropdown
        applicant = enrichUserWithCompanyName(applicant);

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
            case SHORTLISTED:
                return ApplicationStatus.SHORTLISTED;
            case REJECTED:
                return ApplicationStatus.REJECTED;
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

    @Transactional
    public JobApplicationResponse switchReferrer(Long applicationId, Long newJobReferrerId, User applicant) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!application.getApplicantId().equals(applicant.getId())) {
            throw new RuntimeException("You are not authorized to modify this application");
        }
        if (application.getStatus() != JobApplication.ApplicationStatus.PENDING) {
            throw new RuntimeException("You cannot switch referrer after your application is already shared with the other referrer");
        }
        application.setJobReferrerId(newJobReferrerId);
        application = jobApplicationRepository.save(application);
        return mapToBasicResponse(application);
    }
} 