package com.tymbl.registration.dto;

import com.tymbl.common.entity.Role;
import com.tymbl.common.entity.User;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class RegisterRequest {
    
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must be at least 8 characters long and contain at least one digit, one uppercase letter, one lowercase letter, and one special character")
    private String password;
    
    private String firstName;
    private String lastName;
    private Role role;
    private String phoneNumber;
    
    // Professional Details
    private String company;
    
    // Replace strings with entity IDs
    private Long designationId;
    private Long departmentId;
    private Long cityId;
    private Long countryId;
    private String zipCode;
    
    private String linkedInProfile;
    private String portfolioUrl;
    private String resumeUrl;
    
    // Experience
    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;
    
    @Min(value = 0, message = "Months of experience cannot be negative")
    @Max(value = 11, message = "Months of experience cannot be more than 11")
    private Integer monthsOfExperience;
    
    private Integer currentSalary;
    private Integer expectedSalary;
    private Integer noticePeriod;
    
    // Skills
    private List<String> skills;
    
    // Education
    private Collection<User.Education> education;
} 