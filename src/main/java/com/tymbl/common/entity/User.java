package com.tymbl.common.entity;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

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
    
    @NotBlank
    @Column(nullable = false)
    private String password;
    
    @NotBlank
    @Column(nullable = false)
    private String firstName;
    
    @NotBlank
    @Column(nullable = false)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    private String phoneNumber;
    
    // Professional Details
    private String company;
    
    // Replace string position with Designation entity reference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id")
    private Designation designation;
    
    // Replace string department with Department entity reference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;
    
    // Replace location with City and Country references
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    private City city;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private Country country;
    
    private String zipCode;
    
    private String linkedInProfile;
    private String portfolioUrl;
    private String resumeUrl;
    
    // Experience
    private Integer yearsOfExperience;
    private String currentSalary;
    private String expectedSalary;
    private String noticePeriod;
    
    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private Collection<String> skills;
    
    @ElementCollection
    @CollectionTable(name = "user_education", joinColumns = @JoinColumn(name = "user_id"))
    private Collection<Education> education;
    
    // OAuth2 related fields
    private String provider; // "local" or "linkedin"
    private String providerId;
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    private boolean emailVerified = false;
    private String emailVerificationToken;
    private String passwordResetToken;
    private Long passwordResetTokenExpiry;
    
    private String profilePicture;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    public static class Education {
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private String startDate;
        private String endDate;
        private String grade;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
        return designation != null ? designation.getTitle() : null;
    }
    
    @Transient
    public String getDepartmentDisplay() {
        return department != null ? department.getName() : null;
    }
    
    @Transient
    public String getLocationDisplay() {
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