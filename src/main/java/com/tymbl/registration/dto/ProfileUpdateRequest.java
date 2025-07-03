package com.tymbl.registration.dto;

import com.tymbl.common.entity.Role;
import com.tymbl.common.entity.User;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Set;
import java.util.HashSet;

@Data
public class ProfileUpdateRequest {
    private String firstName;
    private String lastName;
    
    @JsonIgnore
    private Role role = Role.USER; // Always set to USER, ignore frontend value
    
    private String phoneNumber;
    private String company;
    private Long companyId;
    private Long departmentId;
    private String designation;
    private Long designationId;
    private Long cityId;
    private Long countryId;
    private String zipCode;
    private String linkedInProfile;
    private String githubProfile;
    private String portfolioWebsite;
    private String resume;
    
    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;
    
    @Min(value = 0, message = "Months of experience cannot be negative")
    @Max(value = 11, message = "Months of experience cannot be more than 11")
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