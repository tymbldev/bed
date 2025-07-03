package com.tymbl.registration.service;

import com.tymbl.auth.dto.AuthResponse;
import com.tymbl.auth.service.JwtService;
import com.tymbl.common.entity.Role;
import com.tymbl.common.entity.User;
import com.tymbl.common.entity.UserSkill;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.common.repository.UserSkillRepository;
import com.tymbl.common.service.EmailService;
import com.tymbl.common.service.LinkedInService;
import com.tymbl.exception.EmailAlreadyExistsException;
import com.tymbl.registration.dto.LinkedInProfile;
import com.tymbl.registration.dto.LinkedInRegisterRequest;
import com.tymbl.registration.dto.ProfileUpdateRequest;
import com.tymbl.registration.dto.RegisterRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

  private final UserRepository userRepository;
  private final UserSkillRepository userSkillRepository;
  private final PasswordEncoder passwordEncoder;
  private final LinkedInService linkedInService;
  private final JwtService jwtService;
  private final EmailService emailService;

  public User getUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
  }

  @Transactional
  public User registerUser(RegisterRequest request) {
    try {
      if (userRepository.existsByEmail(request.getEmail())) {
        throw new EmailAlreadyExistsException(request.getEmail());
      }

      User user = new User();
      user.setEmail(request.getEmail());
      user.setPassword(passwordEncoder.encode(request.getPassword()));
      user.setRole(request.getRole() != null ? request.getRole() : com.tymbl.common.entity.Role.USER);

      // Set optional fields if provided
      if (request.getFirstName() != null) {
        user.setFirstName(request.getFirstName());
      }

      if (request.getLastName() != null) {
        user.setLastName(request.getLastName());
      }
      updateUserFields(user, request);

      User savedUser = userRepository.save(user);
      
      // Save skills separately
      saveUserSkills(savedUser.getId(), request.getSkillIds(), request.getSkillNames());

      return savedUser;
    } catch (Exception e) {
      if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
        throw new RuntimeException("Database constraint violation: " + e.getCause().getMessage());
      }
      throw new RuntimeException("Error registering user: " + e.getMessage());
    }
  }

  @Transactional
  public AuthResponse registerUserWithToken(RegisterRequest request) {
    // Check if user already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new EmailAlreadyExistsException(request.getEmail());
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
    try {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));

      // Update the user fields
      updateUserFields(user, request);
      
      User savedUser = userRepository.save(user);
      
      // Handle skills separately
      if (request.getSkillIds() != null || request.getSkillNames() != null) {
        // Delete existing skills
        userSkillRepository.deleteAllByUserId(userId);
        
        // Save new skills
        saveUserSkills(userId, request.getSkillIds(), request.getSkillNames());
      }

      return savedUser;
    } catch (Exception e) {
      if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
        throw new RuntimeException("Database constraint violation: " + e.getCause().getMessage());
      }
      throw new RuntimeException("Error updating user profile: " + e.getMessage());
    }
  }

  private void updateUserFields(User user, Object requestObj) {
    if (requestObj instanceof RegisterRequest) {
      RegisterRequest registerRequest = (RegisterRequest) requestObj;
      user.setFirstName(registerRequest.getFirstName());
      user.setLastName(registerRequest.getLastName());
      user.setPhoneNumber(registerRequest.getPhoneNumber());
      user.setCompany(registerRequest.getCompany());
      user.setCompanyId(registerRequest.getCompanyId());
      user.setDepartmentId(registerRequest.getDepartmentId());
      user.setDesignation(registerRequest.getDesignation());
      user.setDesignationId(registerRequest.getDesignationId());
      user.setCityId(registerRequest.getCityId());
      user.setCountryId(registerRequest.getCountryId());
      user.setZipCode(registerRequest.getZipCode());
      user.setLinkedInProfile(registerRequest.getLinkedInProfile());
      user.setGithubProfile(registerRequest.getGithubProfile());
      user.setPortfolioWebsite(registerRequest.getPortfolioWebsite());
      user.setResume(registerRequest.getResume());
      user.setYearsOfExperience(registerRequest.getYearsOfExperience());
      user.setMonthsOfExperience(registerRequest.getMonthsOfExperience());
      user.setCurrentSalary(registerRequest.getCurrentSalary());
      user.setCurrentSalaryCurrencyId(registerRequest.getCurrentSalaryCurrencyId());
      user.setExpectedSalary(registerRequest.getExpectedSalary());
      user.setExpectedSalaryCurrencyId(registerRequest.getExpectedSalaryCurrencyId());
      user.setNoticePeriod(registerRequest.getNoticePeriod());
      user.setEducation(registerRequest.getEducation());
    } else if (requestObj instanceof ProfileUpdateRequest) {
      ProfileUpdateRequest profileUpdateRequest = (ProfileUpdateRequest) requestObj;
      if (profileUpdateRequest.getFirstName() != null) user.setFirstName(profileUpdateRequest.getFirstName());
      if (profileUpdateRequest.getLastName() != null) user.setLastName(profileUpdateRequest.getLastName());
      if (profileUpdateRequest.getPhoneNumber() != null) user.setPhoneNumber(profileUpdateRequest.getPhoneNumber());
      if (profileUpdateRequest.getCompany() != null) user.setCompany(profileUpdateRequest.getCompany());
      if (profileUpdateRequest.getCompanyId() != null) user.setCompanyId(profileUpdateRequest.getCompanyId());
      if (profileUpdateRequest.getDepartmentId() != null) user.setDepartmentId(profileUpdateRequest.getDepartmentId());
      if (profileUpdateRequest.getDesignation() != null) user.setDesignation(profileUpdateRequest.getDesignation());
      if (profileUpdateRequest.getDesignationId() != null) user.setDesignationId(profileUpdateRequest.getDesignationId());
      if (profileUpdateRequest.getCityId() != null) user.setCityId(profileUpdateRequest.getCityId());
      if (profileUpdateRequest.getCountryId() != null) user.setCountryId(profileUpdateRequest.getCountryId());
      if (profileUpdateRequest.getZipCode() != null) user.setZipCode(profileUpdateRequest.getZipCode());
      if (profileUpdateRequest.getLinkedInProfile() != null) user.setLinkedInProfile(profileUpdateRequest.getLinkedInProfile());
      if (profileUpdateRequest.getGithubProfile() != null) user.setGithubProfile(profileUpdateRequest.getGithubProfile());
      if (profileUpdateRequest.getPortfolioWebsite() != null) user.setPortfolioWebsite(profileUpdateRequest.getPortfolioWebsite());
      if (profileUpdateRequest.getResume() != null) user.setResume(profileUpdateRequest.getResume());
      if (profileUpdateRequest.getYearsOfExperience() != null) user.setYearsOfExperience(profileUpdateRequest.getYearsOfExperience());
      if (profileUpdateRequest.getMonthsOfExperience() != null) user.setMonthsOfExperience(profileUpdateRequest.getMonthsOfExperience());
      if (profileUpdateRequest.getCurrentSalary() != null) user.setCurrentSalary(profileUpdateRequest.getCurrentSalary());
      if (profileUpdateRequest.getCurrentSalaryCurrencyId() != null) user.setCurrentSalaryCurrencyId(profileUpdateRequest.getCurrentSalaryCurrencyId());
      if (profileUpdateRequest.getExpectedSalary() != null) user.setExpectedSalary(profileUpdateRequest.getExpectedSalary());
      if (profileUpdateRequest.getExpectedSalaryCurrencyId() != null) user.setExpectedSalaryCurrencyId(profileUpdateRequest.getExpectedSalaryCurrencyId());
      if (profileUpdateRequest.getNoticePeriod() != null) user.setNoticePeriod(profileUpdateRequest.getNoticePeriod());
      if (profileUpdateRequest.getEducation() != null) user.setEducation(profileUpdateRequest.getEducation());
    }
  }

  @Transactional
  public AuthResponse registerWithLinkedIn(LinkedInRegisterRequest request) {
    // Get LinkedIn profile data
    LinkedInProfile profile = linkedInService.getProfileData(request.getAccessToken());

    // Check if user already exists
    if (userRepository.existsByEmail(profile.getEmail())) {
      throw new EmailAlreadyExistsException(profile.getEmail());
    }

    // Create new user
    User user = new User();
    user.setEmail(profile.getEmail());
    user.setPassword(UUID.randomUUID().toString()); // Generate random password
    user.setRole(Role.USER);
    user.setFirstName(profile.getFirstName());
    user.setLastName(profile.getLastName());
    user.setLinkedInProfile(request.getLinkedInProfileUrl());
    user.setProvider("linkedin");
    user.setProviderId(profile.getId());
    user.setEmailVerified(false);
    user.setEmailVerificationToken(null);

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

  @Transactional(readOnly = true)
  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
  }

  private void saveUserSkills(Long userId, Set<Long> skillIds, Set<String> skillNames) {
    List<UserSkill> skills = new ArrayList<>();
    
    // Add skills with IDs
    if (skillIds != null && !skillIds.isEmpty()) {
      for (Long skillId : skillIds) {
        skills.add(new UserSkill(userId, skillId));
      }
    }
    
    // Add skills with names only
    if (skillNames != null && !skillNames.isEmpty()) {
      for (String skillName : skillNames) {
        skills.add(new UserSkill(userId, skillName));
      }
    }
    
    // Save all skills
    if (!skills.isEmpty()) {
      userSkillRepository.saveAll(skills);
    }
  }
} 