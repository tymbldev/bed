package com.tymbl.jobs.entity;

import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "company_content")
public class CompanyContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "about_us_original", columnDefinition = "LONGTEXT")
    private String aboutUsOriginal;
    
    @Column(name = "culture_original", columnDefinition = "LONGTEXT")
    private String cultureOriginal;

    @Column(name = "content_shortened", nullable = false)
    private boolean contentShortened = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
} 