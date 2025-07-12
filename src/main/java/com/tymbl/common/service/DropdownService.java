package com.tymbl.common.service;

import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Industry;
import com.tymbl.common.entity.Location;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.common.repository.DepartmentRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.common.repository.LocationRepository;
import com.tymbl.common.dto.IndustryStatisticsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DropdownService {

    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final DesignationRepository designationRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final IndustryRepository industryRepository;
    
    // Cache maps for better performance
    private final Map<Long, String> designationCache = new HashMap<>();
    private final Map<Long, String> departmentCache = new HashMap<>();
    private final Map<Long, String> countryCache = new HashMap<>();
    private final Map<Long, String> cityCache = new HashMap<>();
    private final Map<Long, String> industryCache = new HashMap<>();

    // Department methods
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department createDepartment(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new RuntimeException("Department with name '" + department.getName() + "' already exists");
        }
        return departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + id));
    }

    // Location methods
    @Transactional(readOnly = true)
    public List<Location> getAllLocations() {
        List<Location> locations =  locationRepository.findAll();
        return locations;
    }

    @Transactional
    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    @Transactional(readOnly = true)
    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with ID: " + id));
    }

    // Designation methods
    @Transactional(readOnly = true)
    public List<Designation> getAllDesignations() {
        return designationRepository.findAll();
    }

    @Transactional
    public Designation createDesignation(Designation designation) {
        if (designationRepository.existsByName(designation.getName())) {
            throw new RuntimeException("Designation with title '" + designation.getName() + "' already exists");
        }
        return designationRepository.save(designation);
    }

    @Transactional(readOnly = true)
    public Designation getDesignationById(Long id) {
        return designationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found with ID: " + id));
    }
    
    // Country methods
    @Transactional(readOnly = true)
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Country getCountryById(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with ID: " + id));
    }
    
    // City methods
    @Transactional(readOnly = true)
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public City getCityById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with ID: " + id));
    }
    
    // Industry methods
    @Transactional(readOnly = true)
    public List<Industry> getAllIndustries() {
        return industryRepository.findAll();
    }

    @Transactional
    public Industry createIndustry(Industry industry) {
        if (industryRepository.existsByName(industry.getName())) {
            throw new RuntimeException("Industry with name '" + industry.getName() + "' already exists");
        }
        return industryRepository.save(industry);
    }

    @Transactional(readOnly = true)
    public Industry getIndustryById(Long id) {
        return industryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Industry not found with ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public Industry getIndustryByName(String name) {
        return industryRepository.findByName(name)
                .orElse(null);
    }
    
    @Transactional(readOnly = true)
    public Long getIndustryIdByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        Industry industry = getIndustryByName(name);
        return industry != null ? industry.getId() : null;
    }
    
    // Cached methods for enrichment
    @Transactional(readOnly = true)
    public String getDesignationNameById(Long id) {
        if (id == null) return null;
        
        return designationCache.computeIfAbsent(id, designationId -> {
            try {
                Designation designation = designationRepository.findById(designationId).orElse(null);
                return designation != null ? designation.getName() : null;
            } catch (Exception e) {
                return null;
            }
        });
    }
    
    @Transactional(readOnly = true)
    public String getDepartmentNameById(Long id) {
        if (id == null) return null;
        
        return departmentCache.computeIfAbsent(id, departmentId -> {
            try {
                Department department = departmentRepository.findById(departmentId).orElse(null);
                return department != null ? department.getName() : null;
            } catch (Exception e) {
                return null;
            }
        });
    }
    
    @Transactional(readOnly = true)
    public String getCountryNameById(Long id) {
        if (id == null) return null;
        
        return countryCache.computeIfAbsent(id, countryId -> {
            try {
                Country country = countryRepository.findById(countryId).orElse(null);
                return country != null ? country.getName() : null;
            } catch (Exception e) {
                return null;
            }
        });
    }
    
    @Transactional(readOnly = true)
    public String getCityNameById(Long id) {
        if (id == null) return null;
        
        return cityCache.computeIfAbsent(id, cityId -> {
            try {
                City city = cityRepository.findById(cityId).orElse(null);
                return city != null ? city.getName() : null;
            } catch (Exception e) {
                return null;
            }
        });
    }
    
    @Transactional(readOnly = true)
    public String getIndustryNameById(Long id) {
        if (id == null) return null;
        
        return industryCache.computeIfAbsent(id, industryId -> {
            try {
                Industry industry = industryRepository.findById(industryId).orElse(null);
                return industry != null ? industry.getName() : null;
            } catch (Exception e) {
                return null;
            }
        });
    }
    
    // Method to clear cache (useful for testing or when data changes)
    public void clearCache() {
        designationCache.clear();
        departmentCache.clear();
        countryCache.clear();
        cityCache.clear();
        industryCache.clear();
    }
    
    // Industry statistics method
    @Transactional(readOnly = true)
    public List<IndustryStatisticsDTO> getIndustryStatistics() {
        List<Object[]> industryStats = industryRepository.getIndustryStatistics();
        
        return industryStats.stream().map(stat -> {
            Long industryId = (Long) stat[0];
            String industryName = (String) stat[1];
            String industryDescription = (String) stat[2];
            Long companyCount = (Long) stat[3];
            
            // Get top companies for this industry
            List<Object[]> topCompaniesData = industryRepository.getTopCompaniesByIndustry(industryId);
            List<IndustryStatisticsDTO.TopCompanyDTO> topCompanies = topCompaniesData.stream()
                .map(companyData -> IndustryStatisticsDTO.TopCompanyDTO.builder()
                    .companyId((Long) companyData[0])
                    .companyName((String) companyData[1])
                    .logoUrl((String) companyData[2])
                    .website((String) companyData[3])
                    .headquarters((String) companyData[4])
                    .activeJobCount((Long) companyData[5])
                    .build())
                .limit(5) // Limit to top 5 companies
                .collect(Collectors.toList());
            
            return IndustryStatisticsDTO.builder()
                .industryId(industryId)
                .industryName(industryName)
                .industryDescription(industryDescription)
                .companyCount(companyCount)
                .topCompanies(topCompanies)
                .build();
        }).collect(Collectors.toList());
    }
} 