package com.tymbl.jobs.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "posted_by_id", nullable = false)
    private Long postedById;

    private String location;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "salary_range")
    private String salaryRange;

    @Column(name = "experience_required")
    private String experienceRequired;

    @Column(name = "skills_required")
    private String skillsRequired;

    @Column(name = "application_url")
    private String applicationUrl;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
} 