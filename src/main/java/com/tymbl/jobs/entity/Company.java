package com.tymbl.jobs.entity;

import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String website;

    private String logoUrl;

    private String aboutUs;
    private String vision;
    private String mission;
    private String culture;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "headquarters")
    private String headquarters;

    @Column(name = "industry")
    private String industry;

    @Column(name = "company_size")
    private String companySize;

    @Column(name = "specialties")
    private String specialties;

    @Column(name = "is_crawled", nullable = false)
    private boolean isCrawled = false;

    @Column(name = "last_crawled_at")
    private LocalDateTime lastCrawledAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
} 