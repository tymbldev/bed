package com.tymbl.common.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "referrer_feedback", uniqueConstraints = {@UniqueConstraint(columnNames = {"job_referrer_id", "applicant_id"})})
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