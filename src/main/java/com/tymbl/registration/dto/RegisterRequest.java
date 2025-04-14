package com.tymbl.registration.dto;

import com.tymbl.common.entity.Role;
import com.tymbl.common.entity.User;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private String firstName;
    private String lastName;
    private Role role;
    private String phoneNumber;
    
    // Professional Details
    private String company;
    
    // Entity IDs
    private Long designationId;
    private Long departmentId;
    private Long cityId;
    private Long countryId;
    private String zipCode;
    
    private String linkedInProfile;
    private String githubProfile;
    private String portfolioWebsite;
    private String resume;
    
    // Experience
    private Integer yearsOfExperience;
    private Integer monthsOfExperience;
    private Integer currentSalary;
    private Long currentSalaryCurrencyId;
    private Integer expectedSalary;
    private Long expectedSalaryCurrencyId;
    private Integer noticePeriod;
    
    // Skills
    private Set<Long> skillIds;
    
    // Education
    private Set<User.Education> education;
} 