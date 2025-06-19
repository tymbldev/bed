package com.tymbl.common.entity;

import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "designation_id")
    private Long designationId;

    @Column(name = "designation")
    private String designation;

    @Column(nullable = false)
    private BigDecimal salary;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "company")
    private String company;

    @Column(name = "posted_by", nullable = false)
    private Long postedById;

    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill_id")
    private Set<Long> skillIds = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "job_tags", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
} 