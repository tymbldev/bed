package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobRequest;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public JobResponse createJob(JobRequest request, User postedBy) {
        Company company;
        if (request.getCompanyId() == 1000) {
            // New company
            company = new Company();
            company.setName(request.getCompanyName());
            company = companyRepository.save(company);
        } else {
            // Existing company
            company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));
        }

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalary(request.getSalary());
        job.setCurrency(request.getCurrency());
        job.setCompany(company);
        job.setPostedBy(postedBy);

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
        return jobRepository.findByPostedByAndActiveTrue(user, pageable)
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

        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
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
        return jobRepository.findBySkills(skills.stream().map(String::toLowerCase).collect(Collectors.toList()), pageable)
            .map(this::mapToResponse);
    }

    public List<JobResponse> getJobsByCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

        return jobRepository.findByCompany(company).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCompanyAndTitle(Long companyId, String title) {
        return jobRepository.findActiveJobsByCompanyAndTitle(companyId, title).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCompanyName(String companyName) {
        return jobRepository.findByCompanyNameContainingIgnoreCase(companyName).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JobResponse> getJobsByCompanyNameAndTitle(String companyName, String title) {
        return jobRepository.findActiveJobsByCompanyNameAndTitle(companyName, title).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, User currentUser) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));

        // Check if user is authorized to update the job
        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this job");
        }

        // Update company if needed
        if (request.getCompanyId() != null && !request.getCompanyId().equals(job.getCompany().getId())) {
            Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));
            job.setCompany(company);
        }

        // Update job details
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalary(request.getSalary());
        job.setCurrency(request.getCurrency());

        job = jobRepository.save(job);
        return mapToResponse(job);
    }

    private JobResponse mapToResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setLocation(job.getLocation());
        response.setEmploymentType(job.getEmploymentType());
        response.setExperienceLevel(job.getExperienceLevel());
        response.setSalary(job.getSalary());
        response.setCurrency(job.getCurrency());
        response.setCompanyId(job.getCompany().getId());
        response.setCompanyName(job.getCompanyName());
        response.setPostedBy(job.getPostedBy().getId());
        response.setActive(job.isActive());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}