package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.JobApplication;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

  List<JobApplication> findByApplicantId(Long applicantId);

  List<JobApplication> findByJobId(Long jobId);

  List<JobApplication> findByJobIdAndApplicantId(Long jobId, Long applicantId);

  List<JobApplication> findByJobIdIn(List<Long> jobIds);

  @Query("SELECT ja FROM JobApplication ja WHERE ja.updatedAt >= :since ORDER BY ja.updatedAt DESC")
  List<JobApplication> findApplicationsWithStatusChangesSince(@Param("since") LocalDateTime since);

  @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobId = :jobId")
  int countByJobId(@Param("jobId") Long jobId);
} 