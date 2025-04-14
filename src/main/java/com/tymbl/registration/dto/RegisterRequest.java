package com.tymbl.registration.dto;

import com.tymbl.common.entity.Role;
import com.tymbl.common.entity.User;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Set;
import java.util.HashSet;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @Size(max = 100, message = "First name must be less than 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name must be less than 100 characters")
    private String lastName;
    
    @JsonIgnore
    private Role role = Role.USER; // Always set to USER, ignore frontend value
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @Size(max = 100, message = "Company name must be less than 100 characters")
    private String company;
    
    private Long departmentId;
    
    @Size(max = 100, message = "Designation must be less than 100 characters")
    private String designation;
    
    private Long designationId;
    
    private Long cityId;
    
    private Long countryId;
    
    @Pattern(regexp = "^[0-9]{5}(?:-[0-9]{4})?$", message = "Invalid zip code format")
    private String zipCode;
    
    @Size(max = 255, message = "LinkedIn profile URL must be less than 255 characters")
    private String linkedInProfile;
    
    @Size(max = 255, message = "GitHub profile URL must be less than 255 characters")
    private String githubProfile;
    
    @Size(max = 255, message = "Portfolio website URL must be less than 255 characters")
    private String portfolioWebsite;
    
    private String resume;
    
    private Integer yearsOfExperience;
    
    @Min(value = 0, message = "Months of experience must be at least 0")
    @Max(value = 11, message = "Months of experience must be less than 12")
    private Integer monthsOfExperience;
    
    private Integer currentSalary;
    
    private Long currentSalaryCurrencyId;
    
    private Integer expectedSalary;
    
    private Long expectedSalaryCurrencyId;
    
    private Integer noticePeriod;
    
    private Set<Long> skillIds = new HashSet<>();
    
    private Set<String> skillNames = new HashSet<>();
    
    private Set<User.Education> education = new HashSet<>();
} 