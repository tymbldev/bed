package com.tymbl.jobs.repository;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJob(Job job);
    List<JobApplication> findByApplicant(User applicant);
    boolean existsByJobAndApplicant(Job job, User applicant);
} 