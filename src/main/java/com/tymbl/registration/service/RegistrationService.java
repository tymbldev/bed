package com.tymbl.registration.service;

import com.tymbl.auth.dto.AuthResponse;
import com.tymbl.auth.service.JwtService;
import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Role;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.common.repository.DepartmentRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.common.service.LinkedInService;
import com.tymbl.registration.dto.LinkedInProfile;
import com.tymbl.registration.dto.LinkedInRegisterRequest;
import com.tymbl.registration.dto.ProfileUpdateRequest;
import com.tymbl.registration.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final DepartmentRepository departmentRepository;
  private final DesignationRepository designationRepository;
  private final CityRepository cityRepository;
  private final CountryRepository countryRepository;
  private final LinkedInService linkedInService;
  private final JwtService jwtService;

  @Transactional
  public User registerUser(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already registered");
    }

    User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(Role.USER);

    // Set optional fields if provided
    if (request.getFirstName() != null) {
      user.setFirstName(request.getFirstName());
    }

    if (request.getLastName() != null) {
      user.setLastName(request.getLastName());
    }
    updateUserFields(user, request);

    return userRepository.save(user);
  }

  @Transactional
  public AuthResponse registerUserWithToken(RegisterRequest request) {
    // Check if user already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already registered");
    }

    // Register the user
    User user = registerUser(request);

    // Generate JWT token
    String token = jwtService.generateToken(user);

    // Return auth response
    return AuthResponse.builder()
        .token(token)
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .role(String.valueOf(user.getRole()))
        .emailVerified(user.isEmailVerified())
        .build();
  }

  @Transactional
  public User updateUserProfile(Long userId, ProfileUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    // Update basic info if provided
    if (request.getFirstName() != null) {
      user.setFirstName(request.getFirstName());
    }

    if (request.getLastName() != null) {
      user.setLastName(request.getLastName());
    }

    if (request.getRole() != null) {
      user.setRole(request.getRole());
    }

    if (request.getPhoneNumber() != null) {
      user.setPhoneNumber(request.getPhoneNumber());
    }

    // Update the rest of the fields
    updateUserFields(user, request);

    return userRepository.save(user);
  }

  private void updateUserFields(User user, Object requestObj) {
    // Handle both RegisterRequest and ProfileUpdateRequest types
    String phoneNumber = null;
    String company = null;
    Long departmentId = null;
    Long designationId = null;
    Long cityId = null;
    Long countryId = null;
    String zipCode = null;
    String linkedInProfile = null;
    String portfolioUrl = null;
    String resumeUrl = null;
    Integer yearsOfExperience = null;
    Integer currentSalary = null;
    Integer expectedSalary = null;
    Integer noticePeriod = null;
    java.util.List<String> skills = null;
    java.util.Collection<User.Education> education = null;

    // Extract fields based on request type
    if (requestObj instanceof RegisterRequest) {
      RegisterRequest request = (RegisterRequest) requestObj;
      phoneNumber = request.getPhoneNumber();
      company = request.getCompany();
      departmentId = request.getDepartmentId();
      designationId = request.getDesignationId();
      cityId = request.getCityId();
      countryId = request.getCountryId();
      zipCode = request.getZipCode();
      linkedInProfile = request.getLinkedInProfile();
      portfolioUrl = request.getPortfolioUrl();
      resumeUrl = request.getResumeUrl();
      yearsOfExperience = request.getYearsOfExperience();
      currentSalary = request.getCurrentSalary();
      expectedSalary = request.getExpectedSalary();
      noticePeriod = request.getNoticePeriod();
      skills = request.getSkills();
      education = request.getEducation();
    } else if (requestObj instanceof ProfileUpdateRequest) {
      ProfileUpdateRequest request = (ProfileUpdateRequest) requestObj;
      phoneNumber = request.getPhoneNumber();
      company = request.getCompany();
      departmentId = request.getDepartmentId();
      designationId = request.getDesignationId();
      cityId = request.getCityId();
      countryId = request.getCountryId();
      zipCode = request.getZipCode();
      linkedInProfile = request.getLinkedInProfile();
      portfolioUrl = request.getPortfolioUrl();
      resumeUrl = request.getResumeUrl();
      yearsOfExperience = request.getYearsOfExperience();
      currentSalary = request.getCurrentSalary();
      expectedSalary = request.getExpectedSalary();
      noticePeriod = request.getNoticePeriod();
      skills = request.getSkills();
      education = request.getEducation();
    }

    // Update fields if provided
    if (phoneNumber != null) {
      user.setPhoneNumber(phoneNumber);
    }

    if (company != null) {
      user.setCompany(company);
    }

    if (zipCode != null) {
      user.setZipCode(zipCode);
    }

    // Set department if ID provided
    if (departmentId != null) {
      Department department = departmentRepository.findById(departmentId)
          .orElseThrow(() -> new RuntimeException("Department not found"));
      user.setDepartment(department);
    }

    // Set designation (position) if ID provided
    if (designationId != null) {
      Designation designation = designationRepository.findById(designationId)
          .orElseThrow(() -> new RuntimeException("Designation not found"));
      user.setDesignation(designation);
    }

    // Set city if ID provided
    if (cityId != null) {
      City city = cityRepository.findById(cityId)
          .orElseThrow(() -> new RuntimeException("City not found"));
      user.setCity(city);
    }

    // Set country if ID provided
    if (countryId != null) {
      Country country = countryRepository.findById(countryId)
          .orElseThrow(() -> new RuntimeException("Country not found"));
      user.setCountry(country);
    }

    // Set additional fields if available
    if (skills != null) {
      user.setSkills(skills);
    }

    if (education != null) {
      user.setEducation(education);
    }

    if (yearsOfExperience != null) {
      user.setYearsOfExperience(yearsOfExperience);
    }

    if (currentSalary != null) {
      user.setCurrentSalary(currentSalary);
    }

    if (expectedSalary != null) {
      user.setExpectedSalary(expectedSalary);
    }

    if (noticePeriod != null) {
      user.setNoticePeriod(noticePeriod);
    }

    if (linkedInProfile != null) {
      user.setLinkedInProfile(linkedInProfile);
    }

    if (portfolioUrl != null) {
      user.setPortfolioUrl(portfolioUrl);
    }

    if (resumeUrl != null) {
      user.setResumeUrl(resumeUrl);
    }
  }

  @Transactional
  public AuthResponse registerWithLinkedIn(LinkedInRegisterRequest request) {
    // Get LinkedIn profile data
    LinkedInProfile profile = linkedInService.getProfileData(request.getAccessToken());

    // Check if user already exists
    if (userRepository.existsByEmail(profile.getEmail())) {
      throw new RuntimeException("Email already registered");
    }

    // Create new user
    User user = new User();
    user.setEmail(profile.getEmail());
    // Generate a random password since it's required but won't be used
    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

    // Set LinkedIn data
    user.setFirstName(profile.getFirstName());
    user.setLastName(profile.getLastName());
    user.setLinkedInProfile(request.getLinkedInProfileUrl());

    // Set default role
    user.setRole(Role.USER);

    // Save user
    user = userRepository.save(user);

    // Generate JWT token
    String token = jwtService.generateToken(user);

    // Return auth response
    return AuthResponse.builder()
        .token(token)
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .role(String.valueOf(user.getRole()))
        .emailVerified(user.isEmailVerified())
        .build();
  }
} 