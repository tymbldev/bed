package com.tymbl.jobs.service;

import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.common.repository.DepartmentRepository;
import com.tymbl.jobs.dto.JobPostRequest;
import com.tymbl.jobs.dto.JobResponse;
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
    private final DepartmentRepository departmentRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

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
        return jobRepository.findByActiveTrue(pageable)
                .map(this::mapJobToResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByUser(User user, Pageable pageable) {
        return jobRepository.findByPostedByAndActiveTrue(user, pageable)
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
        job.setZipCode(request.getZipCode());
        job.setRemote(request.isRemote());
        
        // Set department if ID provided
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
            job.setDepartment(department);
        } else {
            job.setDepartment(null);
        }
        
        // Set city if ID provided
        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found"));
            job.setCity(city);
        } else {
            job.setCity(null);
        }
        
        // Set country if ID provided
        if (request.getCountryId() != null) {
            Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found"));
            job.setCountry(country);
        } else {
            job.setCountry(null);
        }
        
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
        JobResponse.JobResponseBuilder builder = JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .company(job.getCompany())
                .zipCode(job.getZipCode())
                .isRemote(job.isRemote())
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
                .isActive(job.isActive());
        
        // Add department details if available
        if (job.getDepartment() != null) {
            builder.departmentId(job.getDepartment().getId())
                   .departmentName(job.getDepartment().getName());
        }
        
        // Add city details if available
        if (job.getCity() != null) {
            builder.cityId(job.getCity().getId())
                   .cityName(job.getCity().getName());
        }
        
        // Add country details if available
        if (job.getCountry() != null) {
            builder.countryId(job.getCountry().getId())
                   .countryName(job.getCountry().getName());
        }
        
        return builder.build();
    }
}