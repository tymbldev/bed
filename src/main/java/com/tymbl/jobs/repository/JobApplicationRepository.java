package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.JobApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

  List<JobApplication> findByApplicantId(Long applicantId);

  List<JobApplication> findByJobId(Long jobId);

  List<JobApplication> findByJobIdAndApplicantId(Long jobId, Long applicantId);

  List<JobApplication> findByJobIdIn(List<Long> jobIds);
} 