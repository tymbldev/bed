package com.tymbl.common.entity;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(name = "designation_id")
    private Long designationId;
    
    @Column(name = "department_id")
    private Long departmentId;
    
    @Column(name = "city_id")
    private Long cityId;
    
    @Column(name = "country_id")
    private Long countryId;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    // Professional Details
    @Column(name = "company")
    private String company;
    
    @Column(name = "linkedin_profile")
    private String linkedInProfile;
    
    @Column(name = "github_profile")
    private String githubProfile;
    
    @Column(name = "portfolio_website")
    private String portfolioWebsite;
    
    // Experience
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
    
    @Column(name = "current_salary")
    private Integer currentSalary;
    
    @Column(name = "expected_salary")
    private Integer expectedSalary;
    
    @Column(name = "notice_period")
    private Integer noticePeriod;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    // OAuth2 related fields
    @Column(name = "provider")
    private String provider; // "local" or "linkedin"
    
    @Column(name = "provider_id")
    private String providerId;
    
    @Column(name = "email_verified")
    private boolean emailVerified;
    
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    @Column(name = "enabled")
    private boolean enabled = true;
    
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_token_expiry")
    private Long passwordResetTokenExpiry;
    
    @Column(name = "profile_picture")
    private String profilePicture;
    
    @Column(name = "profile_completion_percentage")
    private int profileCompletionPercentage;
    
    @Column(name = "resume")
    private String resume;
    
    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill_id")
    private Set<Long> skillIds = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "user_education", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Education> education = new HashSet<>();
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @Embeddable
    public static class Education {
        @Column(name = "institution", nullable = false)
        private String institution;
        
        @Column(name = "degree", nullable = false)
        private String degree;
        
        @Column(name = "field_of_study")
        private String fieldOfStudy;
        
        @Column(name = "start_date")
        private LocalDateTime startDate;
        
        @Column(name = "end_date")
        private LocalDateTime endDate;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    // Convenience methods for UI display
    @Transient
    public String getPositionDisplay() {
        return designationId != null ? designationId.toString() : null;
    }
    
    @Transient
    public String getDepartmentDisplay() {
        return departmentId != null ? departmentId.toString() : null;
    }
    
    @Transient
    public String getLocationDisplay() {
        StringBuilder location = new StringBuilder();
        
        if (cityId != null) {
            location.append(cityId.toString());
        }
        
        if (countryId != null) {
            if (location.length() > 0) {
                location.append(", ");
            }
            location.append(countryId.toString());
        }
        
        if (zipCode != null && !zipCode.isEmpty()) {
            location.append(" ").append(zipCode);
        }
        
        return location.length() > 0 ? location.toString() : null;
    }
} 