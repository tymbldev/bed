package com.tymbl.common.entity;

import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "jobs")
public class Job {
    
    public enum JobType {
        REMOTE_ONLY("Remote Only"),
        WFO("Work From Office"),
        HYBRID("Hybrid");
        
        private final String displayName;
        
        JobType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
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

    @Column(name = "min_salary", nullable = false)
    private BigDecimal minSalary;

    @Column(name = "max_salary", nullable = false)
    private BigDecimal maxSalary;

    @Column(name = "min_experience")
    private Integer minExperience;

    @Column(name = "max_experience")
    private Integer maxExperience;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobType jobType;

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

    @Column(name = "opening_count", nullable = false)
    private Integer openingCount = 1;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "unique_url", unique = true)
    private String uniqueUrl;

    @Column(name = "platform")
    private String platform;

    @Column(name = "approved", nullable = false)
    private Integer approved = JobApprovalStatus.PENDING.getValue();

    @OneToMany(mappedBy = "job")
    private List<JobReferrer> referrers;
    
    // Helper methods for approval status
    public JobApprovalStatus getApprovalStatus() {
        return JobApprovalStatus.fromValue(this.approved);
    }
    
    public void setApprovalStatus(JobApprovalStatus status) {
        this.approved = status.getValue();
    }
    
    public boolean isApproved() {
        return this.approved == JobApprovalStatus.APPROVED.getValue();
    }
    
    public boolean isPending() {
        return this.approved == JobApprovalStatus.PENDING.getValue();
    }
    
    public boolean isRejected() {
        return this.approved == JobApprovalStatus.REJECTED.getValue();
    }
} 