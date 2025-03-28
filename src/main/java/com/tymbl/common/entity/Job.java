package com.tymbl.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "jobs")
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String title;
    
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @NotBlank
    @Column(nullable = false)
    private String company;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    private City city;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private Country country;
    
    private String zipCode;
    private boolean isRemote;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;
    
    @NotNull
    private Integer minExperience;
    
    private Integer maxExperience;
    
    private String minSalary;
    private String maxSalary;
    
    @ElementCollection
    @CollectionTable(name = "job_required_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private Set<String> requiredSkills;
    
    @ElementCollection
    @CollectionTable(name = "job_qualifications", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "qualification", columnDefinition = "TEXT")
    private Set<String> qualifications;
    
    @ElementCollection
    @CollectionTable(name = "job_responsibilities", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "responsibility", columnDefinition = "TEXT")
    private Set<String> responsibilities;
    
    private String educationRequirement;
    
    @NotBlank
    @Column(nullable = false)
    private String workplaceType;
    
    private boolean remoteAllowed;
    
    @NotNull
    private LocalDateTime applicationDeadline;
    
    @NotNull
    private Integer numberOfOpenings;
    
    private String noticePeriod;
    private String referralBonus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_user_id", nullable = false)
    private User postedBy;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime postedDate;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;
    
    @Column(nullable = false)
    private boolean active = true;
    
    public enum JobType {
        FULL_TIME,
        PART_TIME,
        CONTRACT,
        INTERNSHIP,
        FREELANCE
    }
    
    public enum ExperienceLevel {
        ENTRY,
        JUNIOR,
        MID,
        SENIOR,
        LEAD,
        MANAGER,
        DIRECTOR,
        EXECUTIVE
    }
    
    @Transient
    public String getDepartmentDisplay() {
        return department != null ? department.getName() : null;
    }
    
    @Transient
    public String getLocationDisplay() {
        if (isRemote) {
            return "Remote" + (country != null ? " - " + country.getName() : "");
        }
        
        StringBuilder location = new StringBuilder();
        
        if (city != null) {
            location.append(city.getName());
        }
        
        if (country != null) {
            if (location.length() > 0) {
                location.append(", ");
            }
            location.append(country.getName());
        }
        
        if (zipCode != null && !zipCode.isEmpty()) {
            location.append(" ").append(zipCode);
        }
        
        return location.length() > 0 ? location.toString() : null;
    }
} 