package com.tymbl.registration.service;

import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.common.repository.DepartmentRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.registration.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCompany(request.getCompany());
        user.setZipCode(request.getZipCode());
        
        // Set department if ID provided
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
            user.setDepartment(department);
        }
        
        // Set designation (position) if ID provided
        if (request.getDesignationId() != null) {
            Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new RuntimeException("Designation not found"));
            user.setDesignation(designation);
        }
        
        // Set city if ID provided
        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found"));
            user.setCity(city);
        }
        
        // Set country if ID provided
        if (request.getCountryId() != null) {
            Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found"));
            user.setCountry(country);
        }
        
        // Set additional fields if available
        user.setSkills(request.getSkills());
        user.setEducation(request.getEducation());
        user.setYearsOfExperience(request.getYearsOfExperience());
        user.setCurrentSalary(request.getCurrentSalary());
        user.setExpectedSalary(request.getExpectedSalary());
        user.setNoticePeriod(request.getNoticePeriod());
        user.setLinkedInProfile(request.getLinkedInProfile());
        user.setPortfolioUrl(request.getPortfolioUrl());
        user.setResumeUrl(request.getResumeUrl());

        return userRepository.save(user);
    }
} 