package com.tymbl.jobs.entity;

import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "career_page_url")
    private String careerPageUrl;

    private String logoUrl;
    
    private String aboutUs;
    
    private String vision;
    private String mission;
    
    private String culture;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "headquarters")
    private String headquarters;

    @Column(name = "primary_industry_id")
    private Long primaryIndustryId;

    @Column(name = "secondary_industries", columnDefinition = "TEXT")
    private String secondaryIndustries; // Comma-separated list of industry names

    @Column(name = "company_size")
    private String companySize;

    @Column(name = "specialties")
    private String specialties;

    @Column(name = "is_crawled", nullable = false)
    private boolean isCrawled = false;

    @Column(name = "last_crawled_at")
    private LocalDateTime lastCrawledAt;

    @Column(name = "crawled_data", columnDefinition = "LONGTEXT")
    private String crawledData;

    @Column(name = "ai_error")
    private Boolean aiError;

    @Column(name = "similar_companies_by_name", columnDefinition = "TEXT")
    private String similarCompaniesByName;
    
    @Column(name = "similar_companies_by_id", columnDefinition = "TEXT")
    private String similarCompaniesById;
    
    @Column(name = "similar_companies_processed", nullable = false)
    private boolean similarCompaniesProcessed = false;
    
    @Column(name = "processed_name")
    private String processedName;
    
    @Column(name = "processed_name_generated", nullable = false)
    private boolean processedNameGenerated = false;

    @Column(name = "industry_processed", nullable = false)
    private boolean industryProcessed = false;

    @Column(name = "junk_identified", nullable = false)
    private boolean junkIdentified = false;

    @Column(name = "shortname")
    private String shortname;

    @Column(name = "cleanup_processed", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean cleanupProcessed = false;

    @Column(name = "cleanup_processed_at")
    private LocalDateTime cleanupProcessedAt;

    @Column(name = "is_junk", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isJunk = false;

    @Column(name = "junk_reason", columnDefinition = "TEXT")
    private String junkReason;

    @Column(name = "parent_company_name")
    private String parentCompanyName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
} 