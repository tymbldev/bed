package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.JobApprovalStatus;
import com.tymbl.common.entity.JobReferrer;
import com.tymbl.common.entity.ReferrerFeedback;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.JobReferrerRepository;
import com.tymbl.common.repository.ReferrerFeedbackRepository;
import com.tymbl.common.repository.SkillRepository;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.exception.BadRequestException;
import com.tymbl.exception.ForbiddenException;
import com.tymbl.exception.ResourceNotFoundException;
import com.tymbl.exception.UnauthorizedException;
import com.tymbl.jobs.dto.JobReferrerResponse;
import com.tymbl.jobs.dto.JobRequest;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.dto.JobDetailsWithReferrersResponse;
import com.tymbl.jobs.dto.ReferrerFeedbackRequest;
import com.tymbl.jobs.entity.ApplicationStatus;
import com.tymbl.jobs.repository.JobApplicationRepository;
import com.tymbl.jobs.repository.JobRepository;
import com.tymbl.jobs.entity.JobApplication;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final JobReferrerRepository jobReferrerRepository;
    private final ReferrerFeedbackRepository referrerFeedbackRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    @Value("${referrer.sort.weight.designation:0.3}")
    private double designationWeight;
    @Value("${referrer.sort.weight.accepted:0.3}")
    private double acceptedWeight;
    @Value("${referrer.sort.weight.feedback:0.4}")
    private double feedbackWeight;

    @Transactional
    public JobResponse createJob(JobRequest request, User postedBy) {
        if (postedBy == null) {
            throw new UnauthorizedException("User must be authenticated to create a job");
        }
        if (postedBy.getId() == null) {
            throw new BadRequestException("Invalid user ID");
        }

        // If uniqueUrl and platform are provided, check for existing job
        if (request.getUniqueUrl() != null && request.getPlatform() != null) {
            Job existingJob = jobRepository.findByUniqueUrlAndPlatform(request.getUniqueUrl(), request.getPlatform());
            if (existingJob != null) {
                // Add user as referrer if not already present
                if (jobReferrerRepository.findByJobIdAndUserId(existingJob.getId(), postedBy.getId()) == null) {
                    JobReferrer ref = new JobReferrer();
                    ref.setJob(existingJob);
                    ref.setUser(postedBy);
                    jobReferrerRepository.save(ref);
                }
                throw new ConflictException("Job with this URL and platform already exists.");
            }
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
        job.setOpeningCount(request.getOpeningCount() != null ? request.getOpeningCount() : 1);
        job.setUniqueUrl(request.getUniqueUrl());
        job.setPlatform(request.getPlatform());
        
        // Set approval status based on unique URL and platform
        if (request.getUniqueUrl() != null && request.getPlatform() != null) {
            job.setApprovalStatus(JobApprovalStatus.APPROVED);
        } else {
            job.setApprovalStatus(JobApprovalStatus.PENDING);
        }
        
        // Set tags if provided
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            job.setTags(request.getTags());
        }

        job = jobRepository.save(job);
        // Add poster as referrer
        JobReferrer ref = new JobReferrer();
        ref.setJob(job);
        ref.setUser(postedBy);
        jobReferrerRepository.save(ref);
        return mapToResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getAllActiveJobs(Pageable pageable) {
        return jobRepository.findByActiveTrueAndApproved(JobApprovalStatus.APPROVED.getValue(), pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByUser(User user, Pageable pageable) {
        return jobRepository.findByPostedByIdAndActiveTrue(user.getId(), pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getPendingJobsForSuperAdmin(Pageable pageable) {
        return jobRepository.findByActiveTrueAndApproved(JobApprovalStatus.PENDING.getValue(), pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return mapToResponse(job);
    }

    @Transactional
    public void deleteJob(Long jobId, User currentUser) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getPostedById().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to delete this job");
        }

        // Delete all JobReferrer entries for this job
        jobReferrerRepository.deleteByJobId(jobId);
        
        // Set job as inactive
        job.setActive(false);
        jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, Pageable pageable) {
        return jobRepository.searchApprovedJobs(keyword, pageable)
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
        
        return jobRepository.findBySkillsInAndApproved(skillIds, JobApprovalStatus.APPROVED.getValue(), pageable)
            .map(this::mapToResponse);
    }

    public List<JobResponse> getJobsByCompany(Long companyId) {
        return jobRepository.findByCompanyIdAndApproved(companyId, JobApprovalStatus.APPROVED.getValue()).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCompanyAndTitle(Long companyId, String title) {
        return jobRepository.findActiveJobsByCompanyIdAndTitleAndApproved(companyId, title, JobApprovalStatus.APPROVED.getValue()).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCompanyName(String companyName) {
        return jobRepository.findByCompanyContainingIgnoreCaseAndApproved(companyName, JobApprovalStatus.APPROVED.getValue()).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCompanyNameAndTitle(String companyName, String title) {
        return jobRepository.findActiveJobsByCompanyAndTitleAndApproved(companyName, title, JobApprovalStatus.APPROVED.getValue()).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, User currentUser) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Check if user is authorized to update the job
        if (!job.getPostedById().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to update this job");
        }

        // Prevent updating company and designation after job is posted
        if (request.getCompanyId() != null && !request.getCompanyId().equals(job.getCompanyId())) {
            throw new BadRequestException("Cannot update company after job is posted");
        }
        
        if (request.getCompany() != null && !request.getCompany().equals(job.getCompany())) {
            throw new BadRequestException("Cannot update company after job is posted");
        }
        
        if (request.getDesignationId() != null && !request.getDesignationId().equals(job.getDesignationId())) {
            throw new BadRequestException("Cannot update designation after job is posted");
        }
        
        if (request.getDesignation() != null && !request.getDesignation().equals(job.getDesignation())) {
            throw new BadRequestException("Cannot update designation after job is posted");
        }

        // Update job details (excluding company and designation)
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCityId(request.getCityId());
        job.setCountryId(request.getCountryId());
        job.setSalary(request.getSalary());
        job.setCurrencyId(request.getCurrencyId());
        job.setOpeningCount(request.getOpeningCount() != null ? request.getOpeningCount() : job.getOpeningCount());
        
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

    public List<JobReferrerResponse> getReferrersForJob(Long jobId) {
        List<JobReferrer> referrers = jobReferrerRepository.findByJobId(jobId);
        List<JobReferrerResponse> responses = new java.util.ArrayList<>();
        for (JobReferrer ref : referrers) {
            User user = ref.getUser();
            JobReferrerResponse dto = new JobReferrerResponse();
            dto.setUserId(user.getId());
            dto.setUserName(user.getFirstName() + (user.getLastName() != null ? (" " + user.getLastName()) : ""));
            dto.setDesignation(user.getDesignation());
            // Applications accepted: count of job applications for this job where referrer is this user
            int numApplicationsAccepted = (int) jobApplicationRepository.findByJobId(jobId).stream()
                .filter(app -> app.getJobId().equals(jobId) && app.getJobReferrerId() != null && app.getJobReferrerId().equals(ref.getId()) && app.getStatus() == JobApplication.ApplicationStatus.SHORTLISTED)
                .count();
            dto.setNumApplicationsAccepted(numApplicationsAccepted);
            // Feedback score: average score from feedbacks for this referrer
            double feedbackScore = referrerFeedbackRepository.findByJobReferrerId(ref.getId()).stream()
                .filter(fb -> fb.getScore() != null)
                .mapToInt(ReferrerFeedback::getScore)
                .average().orElse(0.0);
            dto.setFeedbackScore(feedbackScore);
            // Designation score: simple mapping (e.g., higher for 'Manager', 'Lead', etc.)
            double designationScore = getDesignationScore(user.getDesignation());
            // Overall score (configurable weights)
            double overallScore = designationWeight * designationScore + acceptedWeight * numApplicationsAccepted + feedbackWeight * feedbackScore;
            dto.setOverallScore(overallScore);
            responses.add(dto);
        }
        // Sort by overallScore descending
        responses.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        return responses;
    }

    private double getDesignationScore(String designation) {
        if (designation == null) return 0.0;
        String d = designation.toLowerCase();
        if (d.contains("lead")) return 3.0;
        if (d.contains("manager")) return 4.0;
        if (d.contains("director")) return 5.0;
        if (d.contains("head")) return 4.5;
        if (d.contains("principal")) return 4.2;
        if (d.contains("senior")) return 2.0;
        if (d.contains("junior")) return 1.0;
        return 1.5;
    }

    public void submitReferrerFeedback(ReferrerFeedbackRequest request, User applicant) {
        JobReferrer referrer = jobReferrerRepository.findByJobIdAndUserId(request.getJobId(), request.getReferrerUserId());
        if (referrer == null) throw new ResourceNotFoundException("Referrer not found for this job");
        ReferrerFeedback feedback = referrerFeedbackRepository.findByJobReferrerIdAndApplicantId(referrer.getId(), applicant.getId());
        if (feedback == null) feedback = new ReferrerFeedback();
        feedback.setJobReferrer(referrer);
        feedback.setApplicant(applicant);
        feedback.setFeedbackText(request.getFeedbackText());
        feedback.setGotCall(request.getGotCall());
        feedback.setScore(request.getScore());
        referrerFeedbackRepository.save(feedback);
    }

    @Transactional
    public void acceptReferralAsReferrer(Long applicationId, User currentUser, ApplicationStatus status) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        // Only allow if user is job poster or from same company
        if (!job.getPostedById().equals(currentUser.getId()) &&
            (currentUser.getCompanyId() == null || !currentUser.getCompanyId().equals(job.getCompanyId()))) {
            throw new ForbiddenException("You are not authorized to accept this referral application");
        }
        // Add as referrer if not already
        JobReferrer ref = jobReferrerRepository.findByJobIdAndUserId(job.getId(), currentUser.getId());
        if (ref == null) {
            ref = new JobReferrer();
            ref.setJob(job);
            ref.setUser(currentUser);
            jobReferrerRepository.save(ref);
        }
        // Set this user as the referrer for the application
        application.setJobReferrerId(ref.getId());
        
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
            throw new BadRequestException("Invalid status. Only SHORTLISTED or REJECTED allowed.");
        }
        
        application.setStatus(jobApplicationStatus);
        jobApplicationRepository.save(application);
    }

    @Transactional
    public JobResponse approveJob(Long jobId, JobApprovalStatus status, String rejectionReason) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        job.setApprovalStatus(status);
        jobRepository.save(job);
        
        return mapToResponse(job);
    }

    @Transactional
    public void registerAsJobReferrer(Long jobId, User currentUser) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Check if job is active and approved
        if (!job.isActive()) {
            throw new BadRequestException("Cannot register as referrer for inactive job");
        }

        if (!job.isApproved()) {
            throw new BadRequestException("Cannot register as referrer for unapproved job");
        }

        // Check if user is already a referrer for this job
        JobReferrer existingReferrer = jobReferrerRepository.findByJobIdAndUserId(jobId, currentUser.getId());
        if (existingReferrer != null) {
            throw new ConflictException("You are already registered as a referrer for this job");
        }

        // Check if user is from the same company as the job
        if (currentUser.getCompanyId() == null || !currentUser.getCompanyId().equals(job.getCompanyId())) {
            throw new ForbiddenException("You can only register as a referrer for jobs from your company");
        }

        // Create new JobReferrer entry
        JobReferrer jobReferrer = new JobReferrer();
        jobReferrer.setJob(job);
        jobReferrer.setUser(currentUser);
        jobReferrerRepository.save(jobReferrer);
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
        
        // Map openingCount to response
        response.setOpeningCount(job.getOpeningCount());
        
        // Map new fields
        response.setUniqueUrl(job.getUniqueUrl());
        response.setPlatform(job.getPlatform());
        response.setApproved(job.getApproved());
        
        // Count referrers for this job
        int referrerCount = jobReferrerRepository.countByJobId(job.getId());
        response.setReferrerCount(referrerCount);
        
        return response;
    }

    @Transactional(readOnly = true)
    public JobDetailsWithReferrersResponse getJobDetailsWithReferrers(Long jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        JobDetailsWithReferrersResponse response = new JobDetailsWithReferrersResponse();
        
        // Map job details
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
        response.setTags(job.getTags());
        response.setOpeningCount(job.getOpeningCount());
        response.setUniqueUrl(job.getUniqueUrl());
        response.setPlatform(job.getPlatform());
        response.setApproved(job.getApproved());
        response.setApprovalStatus(job.getApprovalStatus().name());
        
        // Get referrers with detailed profiles
        List<JobReferrer> referrers = jobReferrerRepository.findByJobId(jobId);
        List<JobDetailsWithReferrersResponse.JobReferrerWithProfileResponse> referrerResponses = new java.util.ArrayList<>();
        
        for (JobReferrer ref : referrers) {
            User user = ref.getUser();
            JobDetailsWithReferrersResponse.JobReferrerWithProfileResponse referrerResponse = new JobDetailsWithReferrersResponse.JobReferrerWithProfileResponse();
            
            // Basic user info
            referrerResponse.setUserId(user.getId());
            referrerResponse.setUserName(user.getFirstName() + (user.getLastName() != null ? (" " + user.getLastName()) : ""));
            referrerResponse.setEmail(user.getEmail());
            referrerResponse.setDesignation(user.getDesignation());
            referrerResponse.setCompany(user.getCompany());
            referrerResponse.setCompanyId(user.getCompanyId());
            
            // Experience
            referrerResponse.setYearsOfExperience(String.valueOf(user.getYearsOfExperience()));
            referrerResponse.setMonthsOfExperience(String.valueOf(user.getMonthsOfExperience()));
            
            // Education
            if (user.getEducation() != null && !user.getEducation().isEmpty()) {
                String education = user.getEducation().stream()
                    .map(edu -> edu.getDegree() + " from " + edu.getInstitution())
                    .collect(Collectors.joining(", "));
                referrerResponse.setEducation(education);
            }
            
            // Social profiles
            referrerResponse.setPortfolioWebsite(user.getPortfolioWebsite());
            referrerResponse.setLinkedInProfile(user.getLinkedInProfile());
            referrerResponse.setGithubProfile(user.getGithubProfile());
            
            // Referrer metrics
            int numApplicationsAccepted = (int) jobApplicationRepository.findByJobId(jobId).stream()
                .filter(app -> app.getJobId().equals(jobId) && app.getJobReferrerId() != null && app.getJobReferrerId().equals(ref.getId()) && app.getStatus() == JobApplication.ApplicationStatus.SHORTLISTED)
                .count();
            referrerResponse.setNumApplicationsAccepted(numApplicationsAccepted);
            
            double feedbackScore = referrerFeedbackRepository.findByJobReferrerId(ref.getId()).stream()
                .filter(fb -> fb.getScore() != null)
                .mapToInt(ReferrerFeedback::getScore)
                .average().orElse(0.0);
            referrerResponse.setFeedbackScore(feedbackScore);
            
            double designationScore = getDesignationScore(user.getDesignation());
            double overallScore = designationWeight * designationScore + acceptedWeight * numApplicationsAccepted + feedbackWeight * feedbackScore;
            referrerResponse.setOverallScore(overallScore);
            
            // Registration date (using created timestamp from JobReferrer if available)
            referrerResponse.setRegisteredAt(ref.getCreatedAt() != null ? ref.getCreatedAt() : job.getCreatedAt());
            
            referrerResponses.add(referrerResponse);
        }
        
        // Sort referrers by overall score descending
        referrerResponses.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        
        response.setReferrers(referrerResponses);
        response.setReferrerCount(referrerResponses.size());
        
        return response;
    }
}