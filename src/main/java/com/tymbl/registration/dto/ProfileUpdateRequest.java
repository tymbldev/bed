package com.tymbl.registration.dto;

import com.tymbl.common.entity.Role;
import com.tymbl.common.entity.User;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Data
public class ProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private Role role;
    private String phoneNumber;
    private String company;
    private Long departmentId;
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
    private Integer expectedSalary;
    private Integer noticePeriod;
    private Set<Long> skillIds;
    private Set<User.Education> education;
} 