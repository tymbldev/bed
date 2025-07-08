package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.jobs.dto.JobApplicationRequest;
import com.tymbl.jobs.dto.JobApplicationResponse;
import com.tymbl.jobs.dto.JobApplicationResponseExtendedDetails;
import com.tymbl.jobs.entity.ApplicationStatus;
import com.tymbl.jobs.entity.JobApplication;
import com.tymbl.common.util.UserEnrichmentUtil;
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
    private final UserEnrichmentUtil userEnrichmentUtil;

    /**
     * Helper method to enrich user data with all names (company, designation, department, country, city)
     */
    private User enrichUserWithAllNames(User user) {
        return userEnrichmentUtil.enrichUserWithAllNames(user);
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

        // Enrich applicant data with all names (company, designation, department, country, city)
        applicant = enrichUserWithAllNames(applicant);

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
                // Enrich referrer data with all names (company, designation, department, country, city)
                refUser = enrichUserWithAllNames(refUser);
                
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

        // Enrich applicant data with all names (company, designation, department, country, city)
        applicant = enrichUserWithAllNames(applicant);

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

        details.setCreatedAt(application.getCreatedAt());

        // Applicant basic info
        details.setApplicantId(applicant.getId());
        details.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
        details.setApplicantEmail(applicant.getEmail());
        details.setApplicantPhoneNumber(applicant.getPhoneNumber());
        
        // Applicant professional details
        details.setApplicantCompany(applicant.getCompany());
        details.setApplicantCompanyId(applicant.getCompanyId());
        details.setApplicantDesignationId(applicant.getDesignationId());
        details.setApplicantDesignation(applicant.getDesignation());
        details.setApplicantDepartmentId(applicant.getDepartmentId());
        details.setApplicantDepartmentName(applicant.getDepartmentName());
        details.setApplicantCityId(applicant.getCityId());
        details.setApplicantCityName(applicant.getCityName());
        details.setApplicantCountryId(applicant.getCountryId());
        details.setApplicantCountryName(applicant.getCountryName());
        details.setApplicantZipCode(applicant.getZipCode());
        
        // Applicant experience and salary details
        details.setApplicantYearsOfExperience(applicant.getYearsOfExperience());
        details.setApplicantMonthsOfExperience(applicant.getMonthsOfExperience());
        details.setApplicantCurrentSalary(applicant.getCurrentSalary());
        details.setApplicantCurrentSalaryCurrencyId(applicant.getCurrentSalaryCurrencyId());
        details.setApplicantExpectedSalary(applicant.getExpectedSalary());
        details.setApplicantExpectedSalaryCurrencyId(applicant.getExpectedSalaryCurrencyId());
        details.setApplicantNoticePeriod(applicant.getNoticePeriod());
        
        // Applicant social profiles and resume
        details.setApplicantPortfolioUrl(applicant.getPortfolioWebsite());
        details.setApplicantLinkedInUrl(applicant.getLinkedInProfile());
        details.setApplicantGithubUrl(applicant.getGithubProfile());
        details.setApplicantResume(applicant.getResume());
        details.setApplicantResumeContentType(applicant.getResumeContentType());
        
        // Applicant skills and education
        details.setApplicantSkillIds(new ArrayList<>(applicant.getSkillIds()));
        details.setApplicantSkillNames(applicant.getSkillNames());
        details.setApplicantEducationDetails(applicant.getEducation());
        
        // Applicant account details
        details.setApplicantProvider(applicant.getProvider());
        details.setApplicantProviderId(applicant.getProviderId());
        details.setApplicantEmailVerified(applicant.isEmailVerified());
        details.setApplicantEnabled(applicant.isEnabled());
        details.setApplicantProfilePicture(applicant.getProfilePicture());
        details.setApplicantProfileCompletionPercentage(applicant.getProfileCompletionPercentage());
        details.setApplicantUpdatedAt(applicant.getUpdatedAt());
        
        // Application details
        details.setApplicationStatus(convertStatus(application.getStatus()));
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

    @Transactional
    public JobApplicationResponse acceptOrRejectApplicationByReferrer(Long jobId, Long applicantId, ApplicationStatus status, User referrer) {
        // Find the application by jobId and applicantId
        List<JobApplication> applications = jobApplicationRepository.findByJobIdAndApplicantId(jobId, applicantId);
        if (applications.isEmpty()) {
            throw new RuntimeException("Application not found");
        }
        JobApplication application = applications.get(0); // Should only be one application per job per applicant
        
        // Verify the current user is the referrer for this application
        if (!application.getJobReferrerId().equals(referrer.getId())) {
            throw new RuntimeException("You are not authorized to accept/reject this application");
        }
        
        // Verify the application is in PENDING status
        if (application.getStatus() != JobApplication.ApplicationStatus.PENDING) {
            throw new RuntimeException("Application is not in PENDING status");
        }
        
        // Convert ApplicationStatus to JobApplication.ApplicationStatus
        JobApplication.ApplicationStatus jobApplicationStatus;
        switch (status) {
            case SHORTLISTED:
                jobApplicationStatus = JobApplication.ApplicationStatus.SHORTLISTED;
                break;
            case REJECTED:
                jobApplicationStatus = JobApplication.ApplicationStatus.REJECTED;
                break;
            default:
                throw new RuntimeException("Invalid status. Only SHORTLISTED or REJECTED allowed");
        }
        
        // Update the application status
        application.setStatus(jobApplicationStatus);
        application = jobApplicationRepository.save(application);
        
        return mapToBasicResponse(application);
    }
} 