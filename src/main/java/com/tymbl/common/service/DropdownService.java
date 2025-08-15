package com.tymbl.common.service;

import com.tymbl.common.dto.IndustryWiseCompaniesDTO;
import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.Currency;
import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Industry;
import com.tymbl.common.entity.Location;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.common.repository.CurrencyRepository;
import com.tymbl.common.repository.DepartmentRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.common.repository.LocationRepository;
import com.tymbl.common.util.DesignationNameCleaner;
import com.tymbl.jobs.repository.CompanyRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DropdownService {

  private final DepartmentRepository departmentRepository;
  private final LocationRepository locationRepository;
  private final DesignationRepository designationRepository;
  private final CountryRepository countryRepository;
  private final CityRepository cityRepository;
  private final IndustryRepository industryRepository;
  private final CompanyRepository companyRepository;
  private final CurrencyRepository currencyRepository;
  private final IndustryCacheService industryCacheService;

  @Qualifier("taskExecutor")
  private final Executor taskExecutor;


  @PersistenceContext
  private EntityManager entityManager;

  // Caches for performance
  private final Map<Long, String> designationCache = new ConcurrentHashMap<>();
  private final Map<Long, String> departmentCache = new ConcurrentHashMap<>();
  private final Map<Long, String> countryCache = new ConcurrentHashMap<>();
  private final Map<Long, String> cityCache = new ConcurrentHashMap<>();
  private final Map<Long, String> industryCache = new ConcurrentHashMap<>();
  private final Map<Long, String> companyNameCache = new ConcurrentHashMap<>();
  private final Map<Long, String> currencyNameCache = new ConcurrentHashMap<>();
  private final Map<Long, String> currencySymbolCache = new ConcurrentHashMap<>();
  private List<com.tymbl.jobs.entity.Company> companyList = new ArrayList<>();

  // Entity caches for full objects
  private final Map<Long, Department> departmentEntityCache = new ConcurrentHashMap<>();
  private final Map<Long, Location> locationEntityCache = new ConcurrentHashMap<>();
  private final Map<Long, Designation> designationEntityCache = new ConcurrentHashMap<>();
  private final Map<Long, Industry> industryEntityCache = new ConcurrentHashMap<>();
  private final Map<Long, Country> countryEntityCache = new ConcurrentHashMap<>();
  private final Map<Long, City> cityEntityCache = new ConcurrentHashMap<>();

  @PostConstruct
  public void initializeCaches() {
    // Start background initialization of all caches
    CompletableFuture.runAsync(this::initializeAllCachesInBackground, taskExecutor)
        .exceptionally(throwable -> {
          log.error("Failed to initialize caches in background: {}", throwable.getMessage(),
              throwable);
          return null;
        });
  }

  /**
   * Initialize all caches in background thread
   */
  private void initializeAllCachesInBackground() {
    try {
      log.info("Starting background cache initialization...");

      // Initialize all caches in parallel
      CompletableFuture<Void> designationCacheFuture = CompletableFuture.runAsync(
          this::initializeDesignationCache, taskExecutor);
      CompletableFuture<Void> departmentCacheFuture = CompletableFuture.runAsync(
          this::initializeDepartmentCache, taskExecutor);
      CompletableFuture<Void> locationCacheFuture = CompletableFuture.runAsync(
          this::initializeLocationCache, taskExecutor);
      CompletableFuture<Void> countryCacheFuture = CompletableFuture.runAsync(
          this::initializeCountryCache, taskExecutor);
      CompletableFuture<Void> cityCacheFuture = CompletableFuture.runAsync(
          this::initializeCityCache, taskExecutor);
      CompletableFuture<Void> industryCacheFuture = CompletableFuture.runAsync(
          this::initializeIndustryCache, taskExecutor);
      CompletableFuture<Void> companyCacheFuture = CompletableFuture.runAsync(
          this::initializeCompanyCache, taskExecutor);
      log.info("All caches initialized successfully in background");
    } catch (Exception e) {
      log.error("Error during background cache initialization: {}", e.getMessage(), e);
    }
  }


  // Department methods
  @Transactional(readOnly = true)
  public List<Department> getAllDepartments() {
    // Try to get from cache first
    if (!departmentEntityCache.isEmpty()) {
      List<Department> departments = new ArrayList<>(departmentEntityCache.values());
      // Sort so that entries with "other" in the name come last
      departments.sort((d1, d2) -> {
        boolean d1HasOther = d1.getName().toLowerCase().contains("other");
        boolean d2HasOther = d2.getName().toLowerCase().contains("other");
        if (d1HasOther && !d2HasOther) {
          return 1;
        }
        if (!d1HasOther && d2HasOther) {
          return -1;
        }
        return d1.getName().compareToIgnoreCase(d2.getName());
      });
      return departments;
    }

    // Fallback to database if cache is empty
    log.warn("Department cache is empty, falling back to database");
    List<Department> departments = departmentRepository.findAll();
    // Sort so that entries with "other" in the name come last
    departments.sort((d1, d2) -> {
      boolean d1HasOther = d1.getName().toLowerCase().contains("other");
      boolean d2HasOther = d2.getName().toLowerCase().contains("other");
      if (d1HasOther && !d2HasOther) {
        return 1;
      }
      if (!d1HasOther && d2HasOther) {
        return -1;
      }
      return d1.getName().compareToIgnoreCase(d2.getName());
    });
    return departments;
  }

  @Transactional
  public Department createDepartment(Department department) {
    if (departmentRepository.existsByName(department.getName())) {
      throw new RuntimeException(
          "Department with name '" + department.getName() + "' already exists");
    }
    Department savedDepartment = departmentRepository.save(department);

    // Update cache
    if (savedDepartment.getId() != null && savedDepartment.getName() != null) {
      departmentCache.put(savedDepartment.getId(), savedDepartment.getName());
      departmentEntityCache.put(savedDepartment.getId(), savedDepartment);
    }

    return savedDepartment;
  }

  @Transactional(readOnly = true)
  public Department getDepartmentById(Long id) {
    // Try to get from cache first
    Department department = departmentEntityCache.get(id);
    if (department != null) {
      return department;
    }

    // Fallback to database if not in cache
    log.warn("Department with ID {} not found in cache, falling back to database", id);
    return departmentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Department not found with ID: " + id));
  }

  // Location methods
  @Transactional(readOnly = true)
  public List<Location> getAllLocations() {
    // Try to get from cache first
    if (!locationEntityCache.isEmpty()) {
      List<Location> locations = new ArrayList<>(locationEntityCache.values());
      // Sort so that entries with "other" in the name come last
      locations.sort((l1, l2) -> {
        boolean l1HasOther = l1.getCity().toLowerCase().contains("other");
        boolean l2HasOther = l2.getCity().toLowerCase().contains("other");
        if (l1HasOther && !l2HasOther) {
          return 1;
        }
        if (!l1HasOther && l2HasOther) {
          return -1;
        }
        return l1.getCity().compareToIgnoreCase(l2.getCity());
      });
      return locations;
    }

    // Fallback to database if cache is empty
    log.warn("Location cache is empty, falling back to database");
    List<Location> locations = locationRepository.findAll();
    // Sort so that entries with "other" in the name come last
    locations.sort((l1, l2) -> {
      boolean l1HasOther = l1.getCity().toLowerCase().contains("other");
      boolean l2HasOther = l2.getCity().toLowerCase().contains("other");
      if (l1HasOther && !l2HasOther) {
        return 1;
      }
      if (!l1HasOther && l2HasOther) {
        return -1;
      }
      return l1.getCity().compareToIgnoreCase(l2.getCity());
    });
    return locations;
  }

  @Transactional
  public Location createLocation(Location location) {
    if (locationRepository.existsByDisplayName(location.getDisplayName())) {
      throw new RuntimeException(
          "Location with display name '" + location.getDisplayName() + "' already exists");
    }
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
    // Try to get from cache first
    if (!designationEntityCache.isEmpty()) {
      List<Designation> designations = new ArrayList<>(designationEntityCache.values());
      // Sort so that entries with "other" in the name come last
      designations.sort((d1, d2) -> {
        boolean d1HasOther = d1.getName().toLowerCase().contains("other");
        boolean d2HasOther = d2.getName().toLowerCase().contains("other");
        if (d1HasOther && !d2HasOther) {
          return 1;
        }
        if (!d1HasOther && d2HasOther) {
          return -1;
        }
        return d1.getName().compareToIgnoreCase(d2.getName());
      });
      return designations;
    }

    // Fallback to database if cache is empty
    log.warn("Designation cache is empty, falling back to database");
    List<Designation> designations = designationRepository.findAll();
    // Sort so that entries with "other" in the name come last
    designations.sort((d1, d2) -> {
      boolean d1HasOther = d1.getName().toLowerCase().contains("other");
      boolean d2HasOther = d2.getName().toLowerCase().contains("other");
      if (d1HasOther && !d2HasOther) {
        return 1;
      }
      if (!d1HasOther && d2HasOther) {
        return -1;
      }
      return d1.getName().compareToIgnoreCase(d2.getName());
    });
    return designations;
  }

  @Transactional
  public Designation createDesignation(Designation designation) {
    // Clean and validate the designation name
    String cleanedName = DesignationNameCleaner.cleanAndValidateDesignationName(
        designation.getName());
    if (cleanedName == null) {
      throw new RuntimeException("Invalid designation name: " + designation.getName());
    }

    if (designationRepository.existsByName(cleanedName)) {
      throw new RuntimeException(
          "Designation with title '" + cleanedName + "' already exists");
    }

    designation.setName(cleanedName);
    Designation savedDesignation = designationRepository.save(designation);

    // Update cache
    if (savedDesignation.getId() != null && savedDesignation.getName() != null) {
      designationCache.put(savedDesignation.getId(), savedDesignation.getName());
      designationEntityCache.put(savedDesignation.getId(), savedDesignation);
    }

    return savedDesignation;
  }

  @Transactional(readOnly = true)
  public Designation getDesignationById(Long id) {
    // Try to get from cache first
    Designation designation = designationEntityCache.get(id);
    if (designation != null) {
      return designation;
    }

    // Fallback to database if not in cache
    log.warn("Designation with ID {} not found in cache, falling back to database", id);
    return designationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Designation not found with ID: " + id));
  }

  // Country methods
  @Transactional(readOnly = true)
  public List<Country> getAllCountries() {
    // Try to get from cache first
    if (!countryEntityCache.isEmpty()) {
      List<Country> countries = new ArrayList<>(countryEntityCache.values());
      // Sort so that entries with "other" in the name come last
      countries.sort((c1, c2) -> {
        boolean c1HasOther = c1.getName().toLowerCase().contains("other");
        boolean c2HasOther = c2.getName().toLowerCase().contains("other");
        if (c1HasOther && !c2HasOther) {
          return 1;
        }
        if (!c1HasOther && c2HasOther) {
          return -1;
        }
        return c1.getName().compareToIgnoreCase(c2.getName());
      });
      return countries;
    }

    // Fallback to database if cache is empty
    log.warn("Country cache is empty, falling back to database");
    List<Country> countries = countryRepository.findAll();
    // Sort so that entries with "other" in the name come last
    countries.sort((c1, c2) -> {
      boolean c1HasOther = c1.getName().toLowerCase().contains("other");
      boolean c2HasOther = c2.getName().toLowerCase().contains("other");
      if (c1HasOther && !c2HasOther) {
        return 1;
      }
      if (!c1HasOther && c2HasOther) {
        return -1;
      }
      return c1.getName().compareToIgnoreCase(c2.getName());
    });
    return countries;
  }

  @Transactional(readOnly = true)
  public Country getCountryById(Long id) {
    // Try to get from cache first
    Country country = countryEntityCache.get(id);
    if (country != null) {
      return country;
    }

    // Fallback to database if not in cache
    log.warn("Country with ID {} not found in cache, falling back to database", id);
    return countryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Country not found with ID: " + id));
  }

  // City methods
  @Transactional(readOnly = true)
  public List<City> getAllCities() {
    // Try to get from cache first
    if (!cityEntityCache.isEmpty()) {
      List<City> cities = new ArrayList<>(cityEntityCache.values());
      // Sort so that entries with "other" in the name come last
      cities.sort((c1, c2) -> {
        boolean c1HasOther = c1.getName().toLowerCase().contains("other");
        boolean c2HasOther = c2.getName().toLowerCase().contains("other");
        if (c1HasOther && !c2HasOther) {
          return 1;
        }
        if (!c1HasOther && c2HasOther) {
          return -1;
        }
        return c1.getName().compareToIgnoreCase(c2.getName());
      });
      return cities;
    }

    // Fallback to database if cache is empty
    log.warn("City cache is empty, falling back to database");
    List<City> cities = cityRepository.findAll();
    // Sort so that entries with "other" in the name come last
    cities.sort((c1, c2) -> {
      boolean c1HasOther = c1.getName().toLowerCase().contains("other");
      boolean c2HasOther = c2.getName().toLowerCase().contains("other");
      if (c1HasOther && !c2HasOther) {
        return 1;
      }
      if (!c1HasOther && c2HasOther) {
        return -1;
      }
      return c1.getName().compareToIgnoreCase(c2.getName());
    });
    return cities;
  }

  @Transactional(readOnly = true)
  public City getCityById(Long id) {
    // Try to get from cache first
    City city = cityEntityCache.get(id);
    if (city != null) {
      return city;
    }

    // Fallback to database if not in cache
    log.warn("City with ID {} not found in cache, falling back to database", id);
    return cityRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("City not found with ID: " + id));
  }

  // Industry methods
  @Transactional(readOnly = true)
  public List<Industry> getAllIndustries() {
    // Try to get from cache first
    if (!industryEntityCache.isEmpty()) {
      List<Industry> industries = new ArrayList<>(industryEntityCache.values());
      // Sort by rank first (lower rank values come first), then by "other" logic
      industries.sort((i1, i2) -> {
        // First, sort by rank (lower rank values come first)
        Integer rank1 = i1.getRankOrder() != null ? i1.getRankOrder() : Integer.MAX_VALUE;
        Integer rank2 = i2.getRankOrder() != null ? i2.getRankOrder() : Integer.MAX_VALUE;

        if (!rank1.equals(rank2)) {
          return rank1.compareTo(rank2);
        }

        // If ranks are equal, then sort by "other" logic
        boolean i1HasOther = i1.getName().toLowerCase().contains("other");
        boolean i2HasOther = i2.getName().toLowerCase().contains("other");
        if (i1HasOther && !i2HasOther) {
          return 1;
        }
        if (!i1HasOther && i2HasOther) {
          return -1;
        }

        // If both have "other" or both don't have "other", sort alphabetically
        return i1.getName().compareToIgnoreCase(i2.getName());
      });
      return industries;
    }

    // Fallback to database if cache is empty
    log.warn("Industry cache is empty, falling back to database");
    List<Industry> industries = industryRepository.findAll();
    // Sort by rank first (lower rank values come first), then by "other" logic
    industries.sort((i1, i2) -> {
      // First, sort by rank (lower rank values come first)
      Integer rank1 = i1.getRankOrder() != null ? i1.getRankOrder() : Integer.MAX_VALUE;
      Integer rank2 = i2.getRankOrder() != null ? i2.getRankOrder() : Integer.MAX_VALUE;

      if (!rank1.equals(rank2)) {
        return rank1.compareTo(rank2);
      }

      // If ranks are equal, then sort by "other" logic
      boolean i1HasOther = i1.getName().toLowerCase().contains("other");
      boolean i2HasOther = i2.getName().toLowerCase().contains("other");
      if (i1HasOther && !i2HasOther) {
        return 1;
      }
      if (!i1HasOther && i2HasOther) {
        return -1;
      }

      // If both have "other" or both don't have "other", sort alphabetically
      return i1.getName().compareToIgnoreCase(i2.getName());
    });
    return industries;
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
    // Try to get from cache first
    Industry industry = industryEntityCache.get(id);
    if (industry != null) {
      return industry;
    }

    // Fallback to database if not in cache
    log.warn("Industry with ID {} not found in cache, falling back to database", id);
    return industryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Industry not found with ID: " + id));
  }

  @Transactional(readOnly = true)
  public Industry getIndustryByName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return null;
    }

    // Try to get from cache first
    for (Industry industry : industryEntityCache.values()) {
      if (name.equalsIgnoreCase(industry.getName())) {
        return industry;
      }
    }

    // Fallback to database if not in cache
    log.warn("Industry with name '{}' not found in cache, falling back to database", name);
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

  @Transactional(readOnly = true)
  public Long getCityIdByName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return null;
    }

    // Try to get from cache first
    for (City city : cityEntityCache.values()) {
      if (name.equalsIgnoreCase(city.getName())) {
        return city.getId();
      }
    }

    // Fallback to database if not in cache
    log.warn("City with name '{}' not found in cache, falling back to database", name);
    try {
      City city = cityRepository.findByName(name).orElse(null);
      return city != null ? city.getId() : null;
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public Long getCountryIdByName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return null;
    }

    // Try to get from cache first
    for (Country country : countryEntityCache.values()) {
      if (name.equalsIgnoreCase(country.getName())) {
        return country.getId();
      }
    }

    // Fallback to database if not in cache
    log.warn("Country with name '{}' not found in cache, falling back to database", name);
    try {
      Country country = countryRepository.findByName(name).orElse(null);
      return country != null ? country.getId() : null;
    } catch (Exception e) {
      return null;
    }
  }

  // Cached methods for enrichment
  @Transactional(readOnly = true)
  public String getDesignationNameById(Long id) {
    if (id == null) {
      return null;
    }

    // Try cache first
    String name = designationCache.get(id);
    if (name != null) {
      return name;
    }

    // Fallback to database if cache not initialized
    try {
      Designation designation = designationRepository.findById(id).orElse(null);
      name = designation != null ? designation.getName() : null;
      if (name != null) {
        designationCache.put(id, name);
      }
      return name;
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public String getDepartmentNameById(Long id) {
    if (id == null) {
      return null;
    }

    // Try cache first
    String name = departmentCache.get(id);
    if (name != null) {
      return name;
    }

    // Fallback to database if cache not initialized
    try {
      Department department = departmentRepository.findById(id).orElse(null);
      name = department != null ? department.getName() : null;
      if (name != null) {
        departmentCache.put(id, name);
      }
      return name;
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public String getCountryNameById(Long id) {
    if (id == null) {
      return null;
    }

    // Try cache first
    String name = countryCache.get(id);
    if (name != null) {
      return name;
    }

    // Fallback to database if cache not initialized
    try {
      Country country = countryRepository.findById(id).orElse(null);
      name = country != null ? country.getName() : null;
      if (name != null) {
        countryCache.put(id, name);
      }
      return name;
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public String getCityNameById(Long id) {
    if (id == null) {
      return null;
    }

    // Try cache first
    String name = cityCache.get(id);
    if (name != null) {
      return name;
    }

    // Fallback to database if cache not initialized
    try {
      City city = cityRepository.findById(id).orElse(null);
      name = city != null ? city.getName() : null;
      if (name != null) {
        cityCache.put(id, name);
      }
      return name;
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public String getIndustryNameById(Long id) {
    if (id == null) {
      return null;
    }

    // Try cache first
    String name = industryCache.get(id);
    if (name != null) {
      return name;
    }

    // Fallback to database if cache not initialized
    try {
      Industry industry = industryRepository.findById(id).orElse(null);
      name = industry != null ? industry.getName() : null;
      if (name != null) {
        industryCache.put(id, name);
      }
      return name;
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public String getCurrencyNameById(Long id) {
    if (id == null) {
      return null;
    }

    // Try cache first
    String name = currencyNameCache.get(id);
    if (name != null) {
      return name;
    }

    // Fallback to database if cache not initialized
    try {
      Currency currency = currencyRepository.findById(id).orElse(null);
      name = currency != null ? currency.getName() : null;
      if (name != null) {
        currencyNameCache.put(id, name);
      }
      return name;
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public String getCurrencySymbolById(Long id) {
    if (id == null) {
      return null;
    }

    // Try cache first
    String symbol = currencySymbolCache.get(id);
    if (symbol != null) {
      return symbol;
    }

    // Fallback to database if cache not initialized
    try {
      Currency currency = currencyRepository.findById(id).orElse(null);
      symbol = currency != null ? currency.getSymbol() : null;
      if (symbol != null) {
        currencySymbolCache.put(id, symbol);
      }
      return symbol;
    } catch (Exception e) {
      return null;
    }
  }


  // Method to clear cache (useful for testing or when data changes)
  public void clearCache() {
    designationCache.clear();
    departmentCache.clear();
    countryCache.clear();
    cityCache.clear();
    industryCache.clear();
    companyNameCache.clear();
    currencyNameCache.clear();
    currencySymbolCache.clear();
    companyList.clear();
  }

  /**
   * Initialize company cache in background
   */
  @Transactional(readOnly = true)
  private void initializeCompanyCache() {
    try {
      log.info("Initializing company cache...");
      companyList = companyRepository.findAll();
      // Pre-populate company name cache
      for (com.tymbl.jobs.entity.Company company : companyList) {
        if (company != null && company.getId() != null && company.getName() != null) {
          companyNameCache.put(company.getId(), company.getName());
        }
      }
      log.info("Company cache initialized with {} companies", companyList.size());
    } catch (Exception e) {
      log.error("Error initializing company cache: {}", e.getMessage(), e);
    }
  }

  /**
   * Initialize designation cache in background
   */
  @Transactional(readOnly = true)
  private void initializeDesignationCache() {
    try {
      log.info("Initializing designation cache...");
      List<Designation> designations = designationRepository.findAll();
      for (Designation designation : designations) {
        if (designation != null && designation.getId() != null && designation.getName() != null) {
          designationCache.put(designation.getId(), designation.getName());
          designationEntityCache.put(designation.getId(), designation);
        }
      }
      log.info("Designation cache initialized with {} designations", designations.size());
    } catch (Exception e) {
      log.error("Error initializing designation cache: {}", e.getMessage(), e);
    }
  }

  /**
   * Initialize department cache in background
   */
  @Transactional(readOnly = true)
  private void initializeDepartmentCache() {
    try {
      log.info("Initializing department cache...");
      List<Department> departments = departmentRepository.findAll();
      for (Department department : departments) {
        if (department != null && department.getId() != null && department.getName() != null) {
          departmentCache.put(department.getId(), department.getName());
          departmentEntityCache.put(department.getId(), department);
        }
      }
      log.info("Department cache initialized with {} departments", departments.size());
    } catch (Exception e) {
      log.error("Error initializing department cache: {}", e.getMessage(), e);
    }
  }

  /**
   * Initialize country cache in background
   */
  @Transactional(readOnly = true)
  private void initializeCountryCache() {
    try {
      log.info("Initializing country cache...");
      List<Country> countries = countryRepository.findAll();
      for (Country country : countries) {
        if (country != null && country.getId() != null && country.getName() != null) {
          countryCache.put(country.getId(), country.getName());
          countryEntityCache.put(country.getId(), country);
        }
      }
      log.info("Country cache initialized with {} countries", countries.size());
    } catch (Exception e) {
      log.error("Error initializing country cache: {}", e.getMessage(), e);
    }
  }

  /**
   * Initialize city cache in background
   */
  @Transactional(readOnly = true)
  private void initializeCityCache() {
    try {
      log.info("Initializing city cache...");
      List<City> cities = cityRepository.findAll();
      for (City city : cities) {
        if (city != null && city.getId() != null && city.getName() != null) {
          cityCache.put(city.getId(), city.getName());
          cityEntityCache.put(city.getId(), city);
        }
      }
      log.info("City cache initialized with {} cities", cities.size());
    } catch (Exception e) {
      log.error("Error initializing city cache: {}", e.getMessage(), e);
    }
  }

  /**
   * Initialize location cache in background
   */
  @Transactional(readOnly = true)
  private void initializeLocationCache() {
    try {
      log.info("Initializing location cache...");
      List<Location> locations = locationRepository.findAll();
      for (Location location : locations) {
        if (location != null && location.getId() != null && location.getDisplayName() != null) {
          locationEntityCache.put(location.getId(), location);
        }
      }
      log.info("Location cache initialized with {} locations", locations.size());
    } catch (Exception e) {
      log.error("Error initializing location cache: {}", e.getMessage(), e);
    }
  }

  /**
   * Initialize industry cache in background
   */
  @Transactional(readOnly = true)
  private void initializeIndustryCache() {
    try {
      log.info("Initializing industry cache...");
      List<Industry> industries = industryRepository.findAll();
      for (Industry industry : industries) {
        if (industry != null && industry.getId() != null && industry.getName() != null) {
          industryCache.put(industry.getId(), industry.getName());
          industryEntityCache.put(industry.getId(), industry);
        }
      }
      log.info("Industry cache initialized with {} industries", industries.size());
    } catch (Exception e) {
      log.error("Error initializing industry cache: {}", e.getMessage(), e);
    }
  }

  /**
   * Refresh department cache
   */
  public void refreshDepartmentCache() {
    log.info("Refreshing department cache...");
    departmentEntityCache.clear();
    initializeDepartmentCache();
  }

  /**
   * Refresh location cache
   */
  public void refreshLocationCache() {
    log.info("Refreshing location cache...");
    locationEntityCache.clear();
    initializeLocationCache();
  }

  /**
   * Refresh designation cache
   */
  public void refreshDesignationCache() {
    log.info("Refreshing designation cache...");
    designationEntityCache.clear();
    initializeDesignationCache();
  }

  /**
   * Refresh industry cache
   */
  public void refreshIndustryCache() {
    log.info("Refreshing industry cache...");
    industryEntityCache.clear();
    initializeIndustryCache();
  }

  /**
   * Refresh country cache
   */
  public void refreshCountryCache() {
    log.info("Refreshing country cache...");
    countryEntityCache.clear();
    initializeCountryCache();
  }

  /**
   * Refresh city cache
   */
  public void refreshCityCache() {
    log.info("Refreshing city cache...");
    cityEntityCache.clear();
    initializeCityCache();
  }

  /**
   * Refresh all caches
   */
  public void refreshAllCaches() {
    log.info("Refreshing all caches...");
    refreshDepartmentCache();
    refreshLocationCache();
    refreshDesignationCache();
    refreshIndustryCache();
    refreshCountryCache();
    refreshCityCache();
  }

  /**
   * Initialize company list in memory (legacy method for backward compatibility)
   */
  @Transactional(readOnly = true)
  public void initializeCompanyList() {
    if (companyList.isEmpty()) {
      initializeCompanyCache();
    }
  }

  /**
   * Get company name by ID from in-memory cache
   */
  public String getCompanyNameById(Long id) {
    if (id == null) {
      return null;
    }

    // Try cache first
    String name = companyNameCache.get(id);
    if (name != null) {
      return name;
    }

    // Fallback to database if cache not initialized
    try {
      com.tymbl.jobs.entity.Company company = companyRepository.findById(id).orElse(null);
      name = company != null ? company.getName() : null;
      if (name != null) {
        companyNameCache.put(id, name);
      }
      return name;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Refresh company list (useful when companies are updated)
   */
  @Transactional(readOnly = true)
  public void refreshCompanyList() {
    companyList.clear();
    companyNameCache.clear();

  }


  // Industry statistics method
  @Transactional(readOnly = true)
  public List<IndustryWiseCompaniesDTO> getIndustryStatistics() {
    List<Object[]> industryStats = industryCacheService.getIndustryStatistics();
    List<Object[]> jobCounts = industryRepository.getActiveJobCountsForAllIndustries();
    Map<Long, Long> industryJobCountMap = new java.util.HashMap<>();
    for (Object[] row : jobCounts) {
      Long industryId = row[0] == null ? null : ((Number) row[0]).longValue();
      Long jobCount = row[1] == null ? 0L : ((Number) row[1]).longValue();
      if (industryId != null) {
        industryJobCountMap.put(industryId, jobCount);
      }
    }
    List<IndustryWiseCompaniesDTO> result = industryStats.stream().map(stat -> {
      Long industryId = stat[0] == null ? null : ((Number) stat[0]).longValue();
      String industryName = (String) stat[1];
      String industryDescription = (String) stat[2];
      Integer rankOrder = stat[3] == null ? null : ((Number) stat[3]).intValue();
      Long companyCount = stat[4] == null ? 0L : ((Number) stat[4]).longValue();
      List<Object[]> topCompaniesData = industryRepository.getTopCompaniesByIndustry(industryId);
      List<IndustryWiseCompaniesDTO.TopCompanyDTO> topCompanies = topCompaniesData.stream()
          .map(companyData -> {
            Long companyId = companyData[0] == null ? null : ((Number) companyData[0]).longValue();
            IndustryWiseCompaniesDTO.TopCompanyDTO topCompany = new IndustryWiseCompaniesDTO.TopCompanyDTO();
            topCompany.setCompanyId(companyId);
            topCompany.setCompanyName((String) companyData[1]);
            topCompany.setLogoUrl((String) companyData[2]);
            topCompany.setWebsite((String) companyData[3]);
            topCompany.setHeadquarters((String) companyData[4]);
            topCompany.setActiveJobCount(
                companyData[5] == null ? 0 : ((Number) companyData[5]).intValue());
            return topCompany;
          })
          .limit(5)
          .collect(java.util.stream.Collectors.toList());

      IndustryWiseCompaniesDTO industryDTO = new IndustryWiseCompaniesDTO();
      industryDTO.setIndustryId(industryId);
      industryDTO.setIndustryName(industryName);
      industryDTO.setIndustryDescription(industryDescription);
      industryDTO.setRankOrder(rankOrder);
      industryDTO.setCompanyCount(companyCount.intValue());
      industryDTO.setTopCompanies(topCompanies);

      return industryDTO;
    }).collect(java.util.stream.Collectors.toList());

    // Sort by rank first (lower rank values come first), then by "other" logic
    result.sort((i1, i2) -> {
      // First, sort by rank (lower rank values come first)
      Integer rank1 = i1.getRankOrder() != null ? i1.getRankOrder() : Integer.MAX_VALUE;
      Integer rank2 = i2.getRankOrder() != null ? i2.getRankOrder() : Integer.MAX_VALUE;

      if (!rank1.equals(rank2)) {
        return rank1.compareTo(rank2);
      }

      // If ranks are equal, then sort by "other" logic
      boolean i1HasOther = i1.getIndustryName().toLowerCase().contains("other");
      boolean i2HasOther = i2.getIndustryName().toLowerCase().contains("other");
      if (i1HasOther && !i2HasOther) {
        return 1;
      }
      if (!i1HasOther && i2HasOther) {
        return -1;
      }

      // If both have "other" or both don't have "other", sort alphabetically
      return i1.getIndustryName().compareToIgnoreCase(i2.getIndustryName());
    });

    return result;
  }

  @Transactional(readOnly = true)
  public List<IndustryWiseCompaniesDTO.TopCompanyDTO> getCompaniesByIndustry(Long industryId) {
    List<Object[]> companiesData = industryCacheService.getTopCompaniesByIndustry(industryId);
    return companiesData.stream()
        .map(companyData -> {
          Long companyId = companyData[0] == null ? null : ((Number) companyData[0]).longValue();
          IndustryWiseCompaniesDTO.TopCompanyDTO topCompany = new IndustryWiseCompaniesDTO.TopCompanyDTO();
          topCompany.setCompanyId(companyId);
          topCompany.setCompanyName((String) companyData[1]);
          topCompany.setLogoUrl((String) companyData[2]);
          topCompany.setWebsite((String) companyData[3]);
          topCompany.setHeadquarters((String) companyData[4]);
          topCompany.setActiveJobCount(
              companyData[5] == null ? 0 : ((Number) companyData[5]).intValue());
          return topCompany;
        })
        .collect(java.util.stream.Collectors.toList());
  }

  /**
   * Autosuggest for company names, designation names, and tags
   */
  @Transactional(readOnly = true)
  public List<Map<String, String>> autosuggest(String query) {
    if (query == null || query.trim().length() < 3) {
      return Collections.emptyList();
    }
    String q = query.trim().toLowerCase();
    List<Map<String, String>> result = new java.util.ArrayList<>();

    // Designations
    designationRepository.findAll().stream()
        .filter(d -> d.getName() != null && d.getName().toLowerCase().contains(q))
        .forEach(d -> {
          Map<String, String> map = new HashMap<>();
          map.put("keyword", d.getName());
          map.put("type", "designation");
          result.add(map);
        });

    // Tags (from all jobs, distinct)
    List<String> tags = entityManager
        .createQuery("SELECT DISTINCT t FROM Job j JOIN j.tags t WHERE LOWER(t) LIKE :q",
            String.class)
        .setParameter("q", "%" + q + "%")
        .getResultList();
    tags.forEach(tag -> {
      Map<String, String> map = new HashMap<>();
      map.put("keyword", tag);
      map.put("type", "tag");
      result.add(map);
    });

    return result;
  }
} 