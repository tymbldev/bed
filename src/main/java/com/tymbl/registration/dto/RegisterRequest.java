package com.tymbl.registration.dto;

import com.tymbl.common.entity.Role;
import com.tymbl.common.entity.User.Education;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotNull(message = "Role is required")
    private Role role;
    
    private String phoneNumber;
    
    // Professional Details
    private String company;
    private String position;
    private String department;
    private String location;
    private String linkedInProfile;
    private String portfolioUrl;
    private String resumeUrl;
    
    // Experience
    private Integer yearsOfExperience;
    private String currentSalary;
    private String expectedSalary;
    private String noticePeriod;
    
    // Skills
    private List<String> skills;
    
    // Education
    private List<Education> education;
} 