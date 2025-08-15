package com.tymbl.common.util;

import com.tymbl.common.entity.User;
import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEnrichmentUtil {

  private static final Logger logger = LoggerFactory.getLogger(UserEnrichmentUtil.class);

  private final CompanyService companyService;
  private final DropdownService dropdownService;

  /**
   * Enriches user with company name from CompanyService
   */
  public User enrichUserWithCompanyName(User user) {
    try {
      if (user.getCompanyId() != null) {
        String companyName = companyService.getCompanyById(user.getCompanyId()).getName();
        user.setCompany(companyName);
      }
    } catch (Exception e) {
      logger.warn("Could not fetch company name for companyId: {}. Error: {}", user.getCompanyId(),
          e.getMessage());
    }
    return user;
  }

  /**
   * Enriches user with designation name from DropdownService
   */
  public User enrichUserWithDesignationName(User user) {
    try {
      if (user.getDesignationId() != null) {
        String designationName = dropdownService.getDesignationNameById(user.getDesignationId());
        if (designationName != null) {
          user.setDesignation(designationName);
        }
      }
    } catch (Exception e) {
      logger.warn("Could not fetch designation name for designationId: {}. Error: {}",
          user.getDesignationId(), e.getMessage());
    }
    return user;
  }

  /**
   * Enriches user with department name from DropdownService
   */
  public User enrichUserWithDepartmentName(User user) {
    try {
      if (user.getDepartmentId() != null) {
        String departmentName = dropdownService.getDepartmentNameById(user.getDepartmentId());
        if (departmentName != null) {
          user.setDepartmentName(departmentName);
        }
      }
    } catch (Exception e) {
      logger.warn("Could not fetch department name for departmentId: {}. Error: {}",
          user.getDepartmentId(), e.getMessage());
    }
    return user;
  }

  /**
   * Enriches user with country name from DropdownService
   */
  public User enrichUserWithCountryName(User user) {
    try {
      if (user.getCountryId() != null) {
        String countryName = dropdownService.getCountryNameById(user.getCountryId());
        if (countryName != null) {
          user.setCountryName(countryName);
        }
      }
    } catch (Exception e) {
      logger.warn("Could not fetch country name for countryId: {}. Error: {}", user.getCountryId(),
          e.getMessage());
    }
    return user;
  }

  /**
   * Enriches user with city name from DropdownService
   */
  public User enrichUserWithCityName(User user) {
    try {
      if (user.getCityId() != null) {
        String cityName = dropdownService.getCityNameById(user.getCityId());
        if (cityName != null) {
          user.setCityName(cityName);
        }
      }
    } catch (Exception e) {
      logger.warn("Could not fetch city name for cityId: {}. Error: {}", user.getCityId(),
          e.getMessage());
    }
    return user;
  }

  /**
   * Enriches user with all available names (company, designation, department, country, city)
   */
  public User enrichUserWithAllNames(User user) {
    enrichUserWithCompanyName(user);
    enrichUserWithDesignationName(user);
    enrichUserWithDepartmentName(user);
    enrichUserWithCountryName(user);
    enrichUserWithCityName(user);
    return user;
  }
} 