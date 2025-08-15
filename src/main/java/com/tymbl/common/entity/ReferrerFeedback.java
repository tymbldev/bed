package com.tymbl.common.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(name = "referrer_feedback", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_referrer_id", "applicant_id"})})
public class ReferrerFeedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_referrer_id", nullable = false)
  private JobReferrer jobReferrer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "applicant_id", nullable = false)
  private User applicant;

  @Column(name = "feedback_text")
  private String feedbackText;

  @Column(name = "got_call")
  private Boolean gotCall;

  @Column(name = "score")
  private Integer score;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
} 