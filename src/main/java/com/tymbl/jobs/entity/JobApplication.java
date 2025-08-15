package com.tymbl.jobs.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "job_applications")
public class JobApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "job_id", nullable = false)
  private Long jobId;

  @Column(name = "applicant_id", nullable = false)
  private Long applicantId;

  @Column(name = "job_referrer_id", nullable = false)
  private Long jobReferrerId;

  @Column(columnDefinition = "TEXT")
  private String coverLetter;

  @Column(name = "resume_url")
  private String resumeUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationStatus status = ApplicationStatus.PENDING;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  public enum ApplicationStatus {
    PENDING,
    SHORTLISTED,
    REJECTED
  }
} 