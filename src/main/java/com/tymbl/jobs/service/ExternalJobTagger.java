package com.tymbl.jobs.service;

import com.tymbl.jobs.entity.Company;
import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.SimilarContent;
import com.tymbl.common.entity.SimilarContent.ContentType;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.SimilarContentRepository;
import com.tymbl.common.service.AIRestService;

import com.tymbl.jobs.entity.ExternalJobDetail;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalJobTagger {

  private final CompanyRepository companyRepository;
  private final CityRepository cityRepository;
  private final CountryRepository countryRepository;
  private final DesignationRepository designationRepository;
  private final SimilarContentRepository similarContentRepository;
  private final com.tymbl.common.service.AIRestService aiRestService;
  private final com.tymbl.common.service.DropdownService dropdownService;


  /**
   * Tag external job detail with company, designation, city, and country
   */
  @Transactional
  public TaggingResult tagExternalJob(ExternalJobDetail externalJob) {
    TaggingResult result = new TaggingResult();
    
    try {
      // Tag company
      CompanyTaggingResult companyResult = tagCompany(externalJob.getCompanyName(), externalJob.getId(), externalJob.getPortalName());
      result.setCompanyId(companyResult.getCompanyId());
      result.setCompanyName(companyResult.getCompanyName());
      result.setCompanyConfidence(companyResult.getConfidence());
      
      // Tag designation
      DesignationTaggingResult designationResult = tagDesignation(externalJob.getJobTitle(), externalJob.getId(), externalJob.getPortalName());
      result.setDesignationId(designationResult.getDesignationId());
      result.setDesignationName(designationResult.getDesignationName());
      result.setDesignationConfidence(designationResult.getConfidence());
      
      // Tag city
      CityTaggingResult cityResult = tagCity(externalJob.getCityName(), externalJob.getId(), externalJob.getPortalName());
      result.setCityId(cityResult.getCityId());
      result.setCityName(cityResult.getCityName());
      result.setCityConfidence(cityResult.getConfidence());
      
      // Tag country
      CountryTaggingResult countryResult = tagCountry(externalJob.getCountryName(), externalJob.getId(), externalJob.getPortalName());
      result.setCountryId(countryResult.getCountryId());
      result.setCountryName(countryResult.getCountryName());
      result.setCountryConfidence(countryResult.getConfidence());
      
      log.info("Tagged external job {}: Company={} (ID: {}, Confidence: {}), Designation={} (ID: {}, Confidence: {}), City={} (ID: {}, Confidence: {}), Country={} (ID: {}, Confidence: {})",
          externalJob.getId(),
          result.getCompanyName(), result.getCompanyId(), result.getCompanyConfidence(),
          result.getDesignationName(), result.getDesignationId(), result.getDesignationConfidence(),
          result.getCityName(), result.getCityId(), result.getCityConfidence(),
          result.getCountryName(), result.getCountryId(), result.getCountryConfidence());
      
    } catch (Exception e) {
      log.error("Error tagging external job {}: {}", externalJob.getId(), e.getMessage(), e);
      result.setError(e.getMessage());
    }
    
    return result;
  }

  // ============================================================================
  // MAIN TAGGING METHODS
  // ============================================================================

  /**
   * Tag company name using exact match, like operator, and similar content
   */
  private CompanyTaggingResult tagCompany(String companyName, Long sourceId, String portalName) {
    if (companyName == null || companyName.trim().isEmpty()) {
      return new CompanyTaggingResult();
    }

    String normalizedCompanyName = companyName.trim();

    // 1. Try exact match first
    Optional<Company> exactMatch = companyRepository.findByName(normalizedCompanyName);
    if (exactMatch.isPresent()) {
      Company company = exactMatch.get();
      return new CompanyTaggingResult(company.getId(), company.getName(), 1.0);
    }

    // 2. Try similar content table
    List<SimilarContent> similarCompanies = similarContentRepository.findByTypeAndSearchTerm(
        ContentType.COMPANY, normalizedCompanyName);
    
    if (!similarCompanies.isEmpty()) {
      // Find the best match by confidence score
      SimilarContent bestMatch = similarCompanies.stream()
          .filter(sc -> sc.getSimilarName().equalsIgnoreCase(normalizedCompanyName))
          .max((a, b) -> a.getConfidenceScore().compareTo(b.getConfidenceScore()))
          .orElse(null);
      
      if (bestMatch != null) {
        Optional<Company> company = companyRepository.findByName(bestMatch.getParentName());
        if (company.isPresent()) {
          return new CompanyTaggingResult(
              company.get().getId(), 
              company.get().getName(), 
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 3. Try LIKE operator on companies table
    List<Company> likeMatches = companyRepository.findByNameContainingIgnoreCase(normalizedCompanyName);
    if (!likeMatches.isEmpty()) {
      // Use AI to find the best match
      Company bestMatch = findBestCompanyMatchUsingAI(normalizedCompanyName, likeMatches);
      if (bestMatch != null) {
        // Store this mapping in similar content for future use
        storeCompanyMapping(bestMatch.getName(), normalizedCompanyName, 0.85);
        return new CompanyTaggingResult(bestMatch.getId(), bestMatch.getName(), 0.85);
      }
    }

    // 4. Try string similarity with all companies as intermediate fallback
    List<Company> topSimilarityMatches = findBestCompanyUsingStringSimilarity(normalizedCompanyName);
    if (!topSimilarityMatches.isEmpty()) {
      // Use GenAI to validate and select the best match from top candidates
      Company validatedMatch = validateCompanyMatchWithGenAI(normalizedCompanyName, topSimilarityMatches);
      if (validatedMatch != null) {
        // Store this mapping in similar content for future use
        storeCompanyMapping(validatedMatch.getName(), normalizedCompanyName, 0.75);
        return new CompanyTaggingResult(validatedMatch.getId(), validatedMatch.getName(), 0.75);
      }
    }

    // 5. Try GenAI with all companies as final fallback
    Company genAIMatch = findBestCompanyUsingGenAI(normalizedCompanyName);
    if (genAIMatch != null) {
      return new CompanyTaggingResult(genAIMatch.getId(), genAIMatch.getName(), 0.5);
    }

    // 6. Log NO_MATCH case for future analysis
    log.info("NO_MATCH for company: '{}' (sourceId: {}, portal: {}) - No match found after all tagging strategies", 
        normalizedCompanyName, sourceId, portalName);

    return new CompanyTaggingResult();
  }

  /**
   * Tag designation using exact match, like operator, and similar content
   */
  private DesignationTaggingResult tagDesignation(String jobTitle, Long sourceId, String portalName) {
    if (jobTitle == null || jobTitle.trim().isEmpty()) {
      return new DesignationTaggingResult();
    }

    String normalizedJobTitle = jobTitle.trim();

    // 1. Try exact match first
    Optional<Designation> exactMatch = designationRepository.findByName(normalizedJobTitle);
    if (exactMatch.isPresent()) {
      Designation designation = exactMatch.get();
      return new DesignationTaggingResult(designation.getId(), designation.getName(), 1.0);
    }

    // 2. Try similar content table
    List<SimilarContent> similarDesignations = similarContentRepository.findByTypeAndSearchTerm(
        ContentType.DESIGNATION, normalizedJobTitle);
    
    if (!similarDesignations.isEmpty()) {
      // Find the best match by confidence score
      SimilarContent bestMatch = similarDesignations.stream()
          .filter(sc -> sc.getSimilarName().equalsIgnoreCase(normalizedJobTitle))
          .max((a, b) -> a.getConfidenceScore().compareTo(b.getConfidenceScore()))
          .orElse(null);
      
      if (bestMatch != null) {
        Optional<Designation> designation = designationRepository.findByName(bestMatch.getParentName());
        if (designation.isPresent()) {
          return new DesignationTaggingResult(
              designation.get().getId(), 
              designation.get().getName(), 
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 3. Try LIKE operator on designations table
    List<Designation> likeMatches = designationRepository.findByNameContainingIgnoreCase(normalizedJobTitle);
    if (!likeMatches.isEmpty()) {
      // Use AI to find the best match
      Designation bestMatch = findBestDesignationMatchUsingAI(normalizedJobTitle, likeMatches);
      if (bestMatch != null) {
        // Store this mapping in similar content for future use
        storeDesignationMapping(bestMatch.getName(), normalizedJobTitle, 0.85);
        return new DesignationTaggingResult(bestMatch.getId(), bestMatch.getName(), 0.85);
      }
    }

    // 4. Try GenAI with all designations as fallback
    Designation genAIMatch = findBestDesignationUsingGenAI(normalizedJobTitle);
    if (genAIMatch != null) {
      return new DesignationTaggingResult(genAIMatch.getId(), genAIMatch.getName(), 0.7);
    }

    // 5. Log NO_MATCH case for future analysis
    log.info("NO_MATCH for designation: '{}' (sourceId: {}, portal: {}) - No match found after all tagging strategies", 
        normalizedJobTitle, sourceId, portalName);

    return new DesignationTaggingResult();
  }

  /**
   * Tag city name using exact match, like operator, and similar content
   */
  private CityTaggingResult tagCity(String cityName, Long sourceId, String portalName) {
    if (cityName == null || cityName.trim().isEmpty()) {
      return new CityTaggingResult();
    }

    String normalizedCityName = cityName.trim();

    // 1. Try exact match first (handle potential duplicates)
    Optional<City> exactMatch = findCityByNameSafely(normalizedCityName);
    if (exactMatch.isPresent()) {
      City city = exactMatch.get();
      return new CityTaggingResult(city.getId(), city.getName(), 1.0);
    }

    // 2. Try similar content table
    List<SimilarContent> similarCities = similarContentRepository.findByTypeAndSearchTerm(
        ContentType.CITY, normalizedCityName);
    
    if (!similarCities.isEmpty()) {
      // Find the best match by confidence score
      SimilarContent bestMatch = similarCities.stream()
          .filter(sc -> sc.getSimilarName().equalsIgnoreCase(normalizedCityName))
          .max((a, b) -> a.getConfidenceScore().compareTo(b.getConfidenceScore()))
          .orElse(null);
      
      if (bestMatch != null) {
        Optional<City> cityOpt = findCityByNameSafely(bestMatch.getParentName());
        if (cityOpt.isPresent()) {
          City city = cityOpt.get();
          return new CityTaggingResult(city.getId(), city.getName(), 
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 3. Try LIKE operator for fuzzy matching
    List<City> cities = cityRepository.findByNameContainingIgnoreCaseOrderByNameAsc(normalizedCityName);
    if (!cities.isEmpty()) {
      City bestMatch = findBestCityMatchBySimilarity(normalizedCityName, cities);
      if (bestMatch != null) {
        return new CityTaggingResult(bestMatch.getId(), bestMatch.getName(), 0.8);
      }
    }

    // 4. Try GenAI for advanced matching
    City genAIMatch = findBestCityUsingGenAI(normalizedCityName);
    if (genAIMatch != null) {
      return new CityTaggingResult(genAIMatch.getId(), genAIMatch.getName(), 0.7);
    }

    // 5. Log NO_MATCH case for future analysis
    log.info("NO_MATCH for city: '{}' (sourceId: {}, portal: {}) - No match found after all tagging strategies", 
        normalizedCityName, sourceId, portalName);

    return new CityTaggingResult();
  }

  /**
   * Tag country name using exact match, like operator, and similar content
   */
  private CountryTaggingResult tagCountry(String countryName, Long sourceId, String portalName) {
    if (countryName == null || countryName.trim().isEmpty()) {
      return new CountryTaggingResult();
    }

    String normalizedCountryName = countryName.trim();

    // 1. Try exact match first (handle potential duplicates)
    Optional<Country> exactMatch = findCountryByNameSafely(normalizedCountryName);
    if (exactMatch.isPresent()) {
      Country country = exactMatch.get();
      return new CountryTaggingResult(country.getId(), country.getName(), 1.0);
    }

    // 2. Try similar content table
    List<SimilarContent> similarCountries = similarContentRepository.findByTypeAndSearchTerm(
        ContentType.COUNTRY, normalizedCountryName);
    
    if (!similarCountries.isEmpty()) {
      // Find the best match by confidence score
      SimilarContent bestMatch = similarCountries.stream()
          .filter(sc -> sc.getSimilarName().equalsIgnoreCase(normalizedCountryName))
          .max((a, b) -> a.getConfidenceScore().compareTo(b.getConfidenceScore()))
          .orElse(null);
      
      if (bestMatch != null) {
        Optional<Country> countryOpt = findCountryByNameSafely(bestMatch.getParentName());
        if (countryOpt.isPresent()) {
          Country country = countryOpt.get();
          return new CountryTaggingResult(country.getId(), country.getName(), 
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 3. Try LIKE operator for fuzzy matching
    List<Country> countries = countryRepository.findByNameContainingIgnoreCase(normalizedCountryName);
    if (!countries.isEmpty()) {
      Country bestMatch = findBestCountryMatchBySimilarity(normalizedCountryName, countries);
      if (bestMatch != null) {
        return new CountryTaggingResult(bestMatch.getId(), bestMatch.getName(), 0.8);
      }
    }

    // 4. Try GenAI for advanced matching
    Country genAIMatch = findBestCountryUsingGenAI(normalizedCountryName);
    if (genAIMatch != null) {
      return new CountryTaggingResult(genAIMatch.getId(), genAIMatch.getName(), 0.7);
    }

    // 5. Log NO_MATCH case for future analysis
    log.info("NO_MATCH for country: '{}' (sourceId: {}, portal: {}) - No match found after all tagging strategies", 
        normalizedCountryName, sourceId, portalName);

    return new CountryTaggingResult();
  }

  // ============================================================================
  // HELPER FUNCTIONS
  // ============================================================================

  /**
   * Safely find a city by name, handling potential duplicates
   */
  private Optional<City> findCityByNameSafely(String cityName) {
    try {
      return cityRepository.findByName(cityName);
    } catch (Exception e) {
      log.warn("Multiple cities found with name '{}', using first result", cityName);
      // Try to get the first result using a different approach
      List<City> cities = cityRepository.findByNameContainingIgnoreCaseOrderByNameAsc(cityName);
      if (!cities.isEmpty()) {
        // Find exact match if possible
        for (City city : cities) {
          if (city.getName().equalsIgnoreCase(cityName)) {
            return Optional.of(city);
          }
        }
        // Return first match if no exact match found
        return Optional.of(cities.get(0));
      }
      return Optional.empty();
    }
  }

  /**
   * Validate if a company match makes logical sense
   */
  private boolean isValidCompanyMatch(String inputName, String matchedName) {
    // Convert to lowercase for comparison
    String input = inputName.toLowerCase().trim();
    String matched = matchedName.toLowerCase().trim();
    
    // If they're exactly the same, it's valid
    if (input.equals(matched)) {
      return true;
    }
    
    // Check for clear subsidiary patterns
    if (isSubsidiaryPattern(input, matched)) {
      return true;
    }
    
    // Check for clear abbreviation patterns
    if (isAbbreviationPattern(input, matched)) {
      return true;
    }
    
    // Check for common business suffix differences
    if (isBusinessSuffixDifference(input, matched)) {
      return true;
    }
    
    // If none of the above patterns match, it's likely invalid
    return false;
  }
  
  /**
   * Check if input is a subsidiary of matched company
   */
  private boolean isSubsidiaryPattern(String input, String matched) {
    // Patterns like "Company Name Country" -> "Company Name"
    if (input.contains(matched) && input.length() > matched.length()) {
      String remaining = input.replace(matched, "").trim();
      // Check if remaining part is a location or common subsidiary indicator
      return remaining.matches(".*(india|usa|uk|europe|asia|america|canada|australia|germany|france|japan|china).*") ||
             remaining.matches(".*(pvt|ltd|inc|corp|llc|group|holdings|international|global).*");
    }
    return false;
  }
  
  /**
   * Check if input is an abbreviation of matched company
   */
  private boolean isAbbreviationPattern(String input, String matched) {
    // Common abbreviation patterns
    if (input.length() <= 5 && matched.length() > 10) {
      // Check if input could be initials of matched company
      String[] matchedWords = matched.split("\\s+");
      if (matchedWords.length >= 2) {
        StringBuilder initials = new StringBuilder();
        for (String word : matchedWords) {
          if (word.length() > 0) {
            initials.append(word.charAt(0));
          }
        }
        return input.equals(initials.toString());
      }
    }
    return false;
  }
  
  /**
   * Check if difference is just business suffixes
   */
  private boolean isBusinessSuffixDifference(String input, String matched) {
    // Remove common business suffixes
    String cleanInput = input.replaceAll("\\s+(pvt|ltd|inc|corp|llc|company|group|holdings|limited|incorporated|corporation)\\s*$", "");
    String cleanMatched = matched.replaceAll("\\s+(pvt|ltd|inc|corp|llc|company|group|holdings|limited|incorporated|corporation)\\s*$", "");
    
    return cleanInput.equals(cleanMatched);
  }

  /**
   * Safely find a country by name, handling potential duplicates
   */
  private Optional<Country> findCountryByNameSafely(String countryName) {
    try {
      return countryRepository.findByName(countryName);
    } catch (Exception e) {
      log.warn("Multiple countries found with name '{}', using first result", countryName);
      // Try to get the first result using a different approach
      List<Country> countries = countryRepository.findByNameContainingIgnoreCase(countryName);
      if (!countries.isEmpty()) {
        // Find exact match if possible
        for (Country country : countries) {
          if (country.getName().equalsIgnoreCase(countryName)) {
            return Optional.of(country);
          }
        }
        // Return first match if no exact match found
        return Optional.of(countries.get(0));
      }
      return Optional.empty();
    }
  }

  /**
   * Use AI to find the best company match
   */
  private Company findBestCompanyMatchUsingAI(String searchTerm, List<Company> candidates) {
    try {
      // Simple string similarity matching instead of AI
      Company bestMatch = findBestCompanyMatchBySimilarity(searchTerm, candidates);
      if (bestMatch != null) {
        return bestMatch;
      }
    } catch (Exception e) {
      log.warn("AI matching failed for company '{}': {}", searchTerm, e.getMessage());
    }
    
    // Fallback: return the first candidate if AI fails
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  /**
   * Use AI to find the best designation match
   */
  private Designation findBestDesignationMatchUsingAI(String searchTerm, List<Designation> candidates) {
    try {
      // Simple string similarity matching instead of AI
      Designation bestMatch = findBestDesignationMatchBySimilarity(searchTerm, candidates);
      if (bestMatch != null) {
        return bestMatch;
      }
    } catch (Exception e) {
      log.warn("AI matching failed for designation '{}': {}", searchTerm, e.getMessage());
    }
    
    // Fallback: return the first candidate if AI fails
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  /**
   * Use AI to find the best similar content match
   */
  private SimilarContent findBestSimilarContentMatchUsingAI(String searchTerm, List<SimilarContent> candidates, ContentType type) {
    try {
      // Simple string similarity matching instead of AI
      SimilarContent bestMatch = findBestSimilarContentMatchBySimilarity(searchTerm, candidates);
      if (bestMatch != null) {
        return bestMatch;
      }
    } catch (Exception e) {
      log.warn("AI matching failed for similar content '{}': {}", searchTerm, e.getMessage());
    }
    
    // Fallback: return the first candidate if AI fails
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  /**
   * Use GenAI to find best company match from all companies in the database
   */
  private Company findBestCompanyUsingGenAI(String companyName) {
    try {
      // Get all companies from the cached DropdownService instead of database
      List<com.tymbl.jobs.entity.Company> allCompanies = dropdownService.getAllCompanies();
      if (allCompanies.isEmpty()) {
        log.warn("No companies found in cache for GenAI matching");
        return null;
      }

      // Create a prompt for GenAI
      String prompt = createCompanyMatchingPrompt(companyName, allCompanies);
      
      // Call GenAI service
      String aiResponse = callGenAIService(prompt);
      
      if (aiResponse != null && !aiResponse.trim().isEmpty()) {
        // Parse AI response to find the best match
        com.tymbl.jobs.entity.Company bestMatch = parseGenAICompanyResponse(companyName, allCompanies, aiResponse);
        if (bestMatch != null) {
          double confidence = calculateStringSimilarity(companyName.toLowerCase(), bestMatch.getName().toLowerCase());
          return bestMatch;
        }
      }
      
    } catch (Exception e) {
      log.warn("GenAI matching failed for company '{}': {}", companyName, e.getMessage());
    }
    
    return null;
  }

  /**
   * Use GenAI to find best designation match from all designations in the database
   */
  private Designation findBestDesignationUsingGenAI(String jobTitle) {
    try {
      // Get all designations from the cached DropdownService instead of database
      List<Designation> allDesignations = dropdownService.getAllDesignations();
      if (allDesignations.isEmpty()) {
        log.warn("No designations found in cache for GenAI matching");
        return null;
      }

      // Create a prompt for GenAI
      String prompt = createDesignationMatchingPrompt(jobTitle, allDesignations);
      
      // Call GenAI service
      String aiResponse = callGenAIService(prompt);
      
      if (aiResponse != null && !aiResponse.trim().isEmpty()) {
        // Parse AI response to find the best match
        Designation bestMatch = parseGenAIDesignationResponse(jobTitle, allDesignations, aiResponse);
        if (bestMatch != null) {

          return bestMatch;
        }
      }
      
    } catch (Exception e) {
      log.warn("GenAI matching failed for designation '{}': {}", jobTitle, e.getMessage());
    }
    
    return null;
  }

  /**
   * Find best company match using string similarity
   */
  private Company findBestCompanyMatchBySimilarity(String searchTerm, List<Company> candidates) {
    if (candidates.isEmpty()) return null;
    
    Company bestMatch = null;
    double bestScore = 0.0;
    
    for (Company company : candidates) {
      double score = calculateStringSimilarity(searchTerm.toLowerCase(), company.getName().toLowerCase());
      if (score > bestScore) {
        bestScore = score;
        bestMatch = company;
      }
    }
    
    // Only return if similarity is above threshold
    return bestScore > 0.6 ? bestMatch : null;
  }

  /**
   * Find best designation match using string similarity
   */
  private Designation findBestDesignationMatchBySimilarity(String searchTerm, List<Designation> candidates) {
    if (candidates.isEmpty()) return null;
    
    Designation bestMatch = null;
    double bestScore = 0.0;
    
    for (Designation designation : candidates) {
      double score = calculateStringSimilarity(searchTerm.toLowerCase(), designation.getName().toLowerCase());
      if (score > bestScore) {
        bestScore = score;
        bestMatch = designation;
      }
    }
    
    // Only return if similarity is above threshold
    return bestScore > 0.6 ? bestMatch : null;
  }

  /**
   * Find best similar content match using string similarity
   */
  private SimilarContent findBestSimilarContentMatchBySimilarity(String searchTerm, List<SimilarContent> candidates) {
    if (candidates.isEmpty()) return null;
    
    SimilarContent bestMatch = null;
    double bestScore = 0.0;
    
    for (SimilarContent content : candidates) {
      double score = calculateStringSimilarity(searchTerm.toLowerCase(), content.getSimilarName().toLowerCase());
      if (score > bestScore) {
        bestScore = score;
        bestMatch = content;
      }
    }
    
    // Only return if similarity is above threshold
    return bestScore > 0.6 ? bestMatch : null;
  }

  /**
   * Calculate string similarity using Levenshtein distance
   */
  private double calculateStringSimilarity(String s1, String s2) {
    if (s1.equals(s2)) return 1.0;
    
    int distance = calculateLevenshteinDistance(s1, s2);
    int maxLength = Math.max(s1.length(), s2.length());
    
    return maxLength == 0 ? 1.0 : (1.0 - (double) distance / maxLength);
  }

  /**
   * Calculate Levenshtein distance between two strings
   */
  private int calculateLevenshteinDistance(String s1, String s2) {
    int[][] dp = new int[s1.length() + 1][s2.length() + 1];
    
    for (int i = 0; i <= s1.length(); i++) {
      dp[i][0] = i;
    }
    
    for (int j = 0; j <= s2.length(); j++) {
      dp[0][j] = j;
    }
    
    for (int i = 1; i <= s1.length(); i++) {
      for (int j = 1; j <= s2.length(); j++) {
        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
        }
      }
    }
    
    return dp[s1.length()][s2.length()];
  }

  /**
   * Store company mapping in similar content table
   */
  private void storeCompanyMapping(String parentName, String similarName, double confidence) {
    if (!similarContentRepository.existsByParentNameAndSimilarNameAndType(parentName, similarName, ContentType.COMPANY)) {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setParentName(parentName);
      similarContent.setSimilarName(similarName);
      similarContent.setType(ContentType.COMPANY);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setSource("AI_MAPPING");
      
      similarContentRepository.save(similarContent);
      log.info("Stored new company mapping: '{}' -> '{}' (confidence: {})", similarName, parentName, confidence);
    }
  }

  /**
   * Store designation mapping in similar content table
   */
  private void storeDesignationMapping(String parentName, String similarName, double confidence) {
    if (!similarContentRepository.existsByParentNameAndSimilarNameAndType(parentName, similarName, ContentType.DESIGNATION)) {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setParentName(parentName);
      similarContent.setSimilarName(similarName);
      similarContent.setType(ContentType.DESIGNATION);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setSource("AI_MAPPING");
      
      similarContentRepository.save(similarContent);
      log.info("Stored new designation mapping: '{}' -> '{}' (confidence: {})", similarName, parentName, confidence);
    }
  }

  /**
   * Create a prompt for GenAI to match company names
   */
  private String createCompanyMatchingPrompt(String companyName, List<com.tymbl.jobs.entity.Company> companies) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Given the company name '").append(companyName).append("', ");
    prompt.append("find the BEST MATCHING company from the list below. ");
    prompt.append("Consider the following matching scenarios:\n\n");
    prompt.append("1. **Exact Matches**: Perfect name matches\n");
    prompt.append("2. **Singular/Plural Variations**: Company names with or without 's' at the end\n");
    prompt.append("   Examples: 'Fidelity Investments' → 'Fidelity Investment', 'Goldman Sachs' → 'Goldman Sachs'\n");
    prompt.append("3. **Parent Company**: If the input is a subsidiary, division, or regional office, find the parent company\n");
    prompt.append("   Examples: 'Walmart Global Tech India' → 'Walmart', 'Microsoft India' → 'Microsoft'\n");
    prompt.append("4. **Abbreviations**: Official names vs. common abbreviations\n");
    prompt.append("   Examples: 'IBM' vs 'International Business Machines', 'HP' vs 'Hewlett-Packard'\n");
    prompt.append("5. **Legal vs. Trading Names**: Company's legal name vs. how it's commonly known\n");
    prompt.append("6. **Mergers & Acquisitions**: Company names that changed due to business events\n");
    prompt.append("7. **Regional Variations**: Same company with different names in different regions\n");
    prompt.append("8. **Common Suffixes**: Companies with/without 'Corp', 'Inc', 'LLC', 'Ltd', 'Company', 'Group'\n");
    prompt.append("   Examples: 'Apple Inc' → 'Apple', 'Google LLC' → 'Google', 'Amazon Company' → 'Amazon'\n\n");
    prompt.append("IMPORTANT GUIDELINES:\n");
    prompt.append("- If the input contains the main company name (e.g., 'Walmart' in 'Walmart Global Tech India'), return that company\n");
    prompt.append("- If it's clearly a subsidiary/division of a known company, return the parent company\n");
    prompt.append("- Pay special attention to singular/plural variations - 'Fidelity Investments' should match 'Fidelity Investment'\n");
    prompt.append("- Ignore common business suffixes like 'Inc', 'LLC', 'Corp', 'Company', 'Group' when matching\n");
    prompt.append("- If no clear match exists, respond with 'NO_MATCH'\n");
    prompt.append("- Don't return companies that are just similar in the same industry\n\n");
    prompt.append("Available companies:\n");
    
    int counter = 1;
    for (com.tymbl.jobs.entity.Company company : companies) {
      prompt.append(counter++).append(". ").append(company.getName()).append("\n");
    }
    
    prompt.append("\nRESPONSE INSTRUCTIONS:\n");
    prompt.append("- Return ONLY the exact company name from the numbered list above\n");
    prompt.append("- If no suitable match is found, respond with exactly 'NO_MATCH' (no quotes, no extra text)\n");
    prompt.append("- Do not include any explanations, reasoning, or additional text\n");
    prompt.append("- Do not include the number from the list, only the company name\n");
    prompt.append("- Examples of valid responses: 'Microsoft', 'Walmart', 'NO_MATCH'\n");
    
    return prompt.toString();
  }

  /**
   * Create a prompt for GenAI to match designation names
   */
  private String createDesignationMatchingPrompt(String jobTitle, List<Designation> designations) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Given the job title '").append(jobTitle).append("', ");
    prompt.append("find the BEST MATCHING designation from the list below. ");
    prompt.append("Consider the following matching scenarios:\n\n");
    prompt.append("1. **Exact Matches**: Perfect title matches\n");
    prompt.append("2. **Core Role Variations**: Same job role with different titles\n");
    prompt.append("   Examples: 'Software Engineer' vs 'Software Developer', 'Programmer' vs 'Developer'\n");
    prompt.append("3. **Seniority Levels**: Different levels of the same role\n");
    prompt.append("   Examples: 'Senior Developer' vs 'Developer', 'Lead Engineer' vs 'Engineer'\n");
    prompt.append("4. **Industry Standards**: Official titles vs. company-specific variations\n");
    prompt.append("5. **Regional Variations**: Same role with different names in different regions\n");
    prompt.append("6. **Modern vs. Traditional**: Contemporary titles vs. legacy titles\n\n");
    prompt.append("IMPORTANT GUIDELINES:\n");
    prompt.append("- If the input contains the core role (e.g., 'Developer' in 'Senior Full Stack Developer'), return the closest match\n");
    prompt.append("- Consider that 'Software Engineer' and 'Software Developer' are essentially the same role\n");
    prompt.append("- Seniority differences (Junior/Senior/Lead) should still match the base role\n");
    prompt.append("- If no clear match exists, respond with 'NO_MATCH'\n");
    prompt.append("- Don't return designations that are just similar roles in the same field\n\n");
    prompt.append("Available designations:\n");
    
    int counter = 1;
    for (Designation designation : designations) {
      prompt.append(counter++).append(". ").append(designation.getName()).append("\n");
    }
    
    prompt.append("\nRESPONSE INSTRUCTIONS:\n");
    prompt.append("- Return ONLY the exact designation name from the numbered list above\n");
    prompt.append("- If no suitable match is found, respond with exactly 'NO_MATCH' (no quotes, no extra text)\n");
    prompt.append("- Do not include any explanations, reasoning, or additional text\n");
    prompt.append("- Do not include the number from the list, only the designation name\n");
    prompt.append("- Examples of valid responses: 'Software Engineer', 'Developer', 'NO_MATCH'\n");
    
    return prompt.toString();
  }

  /**
   * Call GenAI service to get response
   */
  private String callGenAIService(String prompt) {
    try {
      // Use AIRestService to call Gemini API
      if (aiRestService != null) {
        // Build request body using AIRestService
        Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);
        
        // Call Gemini API
        ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody, "Company/Designation Matching");
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
          // Parse the response to extract the generated text
          return extractTextFromGeminiResponse(response.getBody());
        } else {
          log.warn("Gemini API call failed with status: {} - {}", response.getStatusCode(), response.getBody());
        }
      }
      
      // Fallback: Use string similarity as a placeholder for GenAI
      log.info("Using string similarity as GenAI fallback for prompt: {}", prompt.substring(0, Math.min(100, prompt.length())));
      return "AI_SERVICE_UNAVAILABLE";
      
    } catch (Exception e) {
      log.warn("Failed to call GenAI service: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Extract text from Gemini API response
   */
  private String extractTextFromGeminiResponse(String responseBody) {
    try {
      // Parse JSON response
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode responseNode = objectMapper.readTree(responseBody);
      
      // Navigate to the text content
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            log.debug("Extracted text from Gemini response: {}", generatedText);
            return generatedText;
          }
        }
      }
      
      log.warn("Unexpected Gemini API response structure: {}", responseBody);
      return null;
      
    } catch (Exception e) {
      log.warn("Failed to parse Gemini response: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Parse GenAI response to find the best company match
   */
  private com.tymbl.jobs.entity.Company parseGenAICompanyResponse(String companyName, List<com.tymbl.jobs.entity.Company> companies, String aiResponse) {
    if (aiResponse == null || aiResponse.trim().isEmpty() || "AI_SERVICE_UNAVAILABLE".equals(aiResponse)) {
      // Fallback to string similarity when AI is not available
      return findBestCompanyMatchBySimilarity(companyName, companies);
    }
    
    // Try to find exact match from AI response
    String normalizedResponse = aiResponse.trim();
    if ("NO_MATCH".equalsIgnoreCase(normalizedResponse)) {
      return null; // No exact match found
    }

    for (com.tymbl.jobs.entity.Company company : companies) {
      if (company.getName().equalsIgnoreCase(normalizedResponse)) {
        return company;
      }
    }
    
    // If no exact match, fallback to string similarity
    return findBestCompanyMatchBySimilarity(companyName, companies);
  }

  /**
   * Parse GenAI response to find the best designation match
   */
  private Designation parseGenAIDesignationResponse(String jobTitle, List<Designation> designations, String aiResponse) {
    if (aiResponse == null || aiResponse.trim().isEmpty() || "AI_SERVICE_UNAVAILABLE".equals(aiResponse)) {
      // Fallback to string similarity when AI is not available
      return findBestDesignationMatchBySimilarity(jobTitle, designations);
    }
    
    // Try to find exact match from AI response
    String normalizedResponse = aiResponse.trim();
    if ("NO_MATCH".equalsIgnoreCase(normalizedResponse)) {
      return null; // No exact match found
    }
    
    for (Designation designation : designations) {
      if (designation.getName().equalsIgnoreCase(normalizedResponse)) {
        return designation;
      }
    }
    
    // If no exact match, fallback to string similarity
    return findBestDesignationMatchBySimilarity(jobTitle, designations);
  }

  // Result classes
  public static class TaggingResult {
    private Long companyId;
    private String companyName;
    private Double companyConfidence;
    private Long designationId;
    private String designationName;
    private Double designationConfidence;
    private Long cityId;
    private String cityName;
    private Double cityConfidence;
    private Long countryId;
    private String countryName;
    private Double countryConfidence;
    private String error;

    // Getters and setters
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public Double getCompanyConfidence() { return companyConfidence; }
    public void setCompanyConfidence(Double companyConfidence) { this.companyConfidence = companyConfidence; }
    
    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }
    
    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }
    
    public Double getDesignationConfidence() { return designationConfidence; }
    public void setDesignationConfidence(Double designationConfidence) { this.designationConfidence = designationConfidence; }
    
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    
    public Double getCityConfidence() { return cityConfidence; }
    public void setCityConfidence(Double cityConfidence) { this.cityConfidence = cityConfidence; }
    
    public Long getCountryId() { return countryId; }
    public void setCountryId(Long countryId) { this.countryId = countryId; }
    
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    
    public Double getCountryConfidence() { return countryConfidence; }
    public void setCountryConfidence(Double countryConfidence) { this.countryConfidence = countryConfidence; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
  }

  public static class CompanyTaggingResult {
    private Long companyId;
    private String companyName;
    private Double confidence;

    public CompanyTaggingResult() {}

    public CompanyTaggingResult(Long companyId, String companyName, Double confidence) {
      this.companyId = companyId;
      this.companyName = companyName;
      this.confidence = confidence;
    }

    // Getters and setters
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
  }

  public static class DesignationTaggingResult {
    private Long designationId;
    private String designationName;
    private Double confidence;

    public DesignationTaggingResult() {}

    public DesignationTaggingResult(Long designationId, String designationName, Double confidence) {
      this.designationId = designationId;
      this.designationName = designationName;
      this.confidence = confidence;
    }

    // Getters and setters
    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }
    
    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
  }

  public static class CityTaggingResult {
    private Long cityId;
    private String cityName;
    private Double confidence;

    public CityTaggingResult() {}

    public CityTaggingResult(Long cityId, String cityName, Double confidence) {
      this.cityId = cityId;
      this.cityName = cityName;
      this.confidence = confidence;
    }

    // Getters and setters
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
  }

  public static class CountryTaggingResult {
    private Long countryId;
    private String countryName;
    private Double confidence;

    public CountryTaggingResult() {}

    public CountryTaggingResult(Long countryId, String countryName, Double confidence) {
      this.countryId = countryId;
      this.countryName = countryName;
      this.confidence = confidence;
    }

    // Getters and setters
    public Long getCountryId() { return countryId; }
    public void setCountryId(Long countryId) { this.countryId = countryId; }
    
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
  }

  /**
   * Find best city match using string similarity
   */
  private City findBestCityMatchBySimilarity(String cityName, List<City> cities) {
    if (cities.isEmpty()) return null;
    
    City bestMatch = null;
    double bestScore = 0.0;
    
    for (City city : cities) {
      double score = calculateStringSimilarity(cityName.toLowerCase(), city.getName().toLowerCase());
      if (score > bestScore) {
        bestScore = score;
        bestMatch = city;
      }
    }
    
    // Only return if similarity is above threshold
    return bestScore > 0.6 ? bestMatch : null;
  }

  /**
   * Find best country match using string similarity
   */
  private Country findBestCountryMatchBySimilarity(String countryName, List<Country> countries) {
    if (countries.isEmpty()) return null;
    
    Country bestMatch = null;
    double bestScore = 0.0;
    
    for (Country country : countries) {
      double score = calculateStringSimilarity(countryName.toLowerCase(), country.getName().toLowerCase());
      if (score > bestScore) {
        bestScore = score;
        bestMatch = country;
      }
    }
    
    // Only return if similarity is above threshold
    return bestScore > 0.6 ? bestMatch : null;
  }

  /**
   * Find best city using GenAI
   */
  private City findBestCityUsingGenAI(String cityName) {
    try {
      List<City> cities = dropdownService.getAllCities();
      if (cities.isEmpty()) {
        return null;
      }

      String prompt = createCityMatchingPrompt(cityName, cities);
      String aiResponse = callGenAIService(prompt);
      
      if (aiResponse == null || aiResponse.trim().isEmpty()) {
        return null;
      }

      City matchedCity = parseGenAICityResponse(aiResponse, cities);
      if (matchedCity != null) {
        // Store the mapping in similar content table
        storeCityMapping(matchedCity.getName(), cityName, 0.9);
      }
      
      return matchedCity;
    } catch (Exception e) {
      log.warn("Error in GenAI city matching for '{}': {}", cityName, e.getMessage());
      return null;
    }
  }

  /**
   * Find best country using GenAI
   */
  private Country findBestCountryUsingGenAI(String countryName) {
    try {
      List<Country> countries = dropdownService.getAllCountries();
      if (countries.isEmpty()) {
        return null;
      }

      String prompt = createCountryMatchingPrompt(countryName, countries);
      String aiResponse = callGenAIService(prompt);
      
      if (aiResponse == null || aiResponse.trim().isEmpty()) {
        return null;
      }

      Country matchedCountry = parseGenAICountryResponse(aiResponse, countries);
      if (matchedCountry != null) {
        // Store the mapping in similar content table
        storeCountryMapping(matchedCountry.getName(), countryName, 0.9);
      }
      
      return matchedCountry;
    } catch (Exception e) {
      log.warn("Error in GenAI country matching for '{}': {}", countryName, e.getMessage());
      return null;
    }
  }

  /**
   * Create a prompt for GenAI to match city names
   */
  private String createCityMatchingPrompt(String cityName, List<City> cities) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Given the city name '").append(cityName).append("', ");
    prompt.append("find the BEST MATCHING city from the list below. ");
    prompt.append("Consider the following matching scenarios:\n\n");
    prompt.append("1. Exact name match\n");
    prompt.append("2. Abbreviations (e.g., 'NYC' matches 'New York City')\n");
    prompt.append("3. Alternative spellings or variations\n");
    prompt.append("4. Regional variations or local names\n");
    prompt.append("5. Historical names or renames\n\n");
    prompt.append("Available cities:\n");
    
    for (int i = 0; i < cities.size(); i++) {
      City city = cities.get(i);
      prompt.append(i + 1).append(". ").append(city.getName()).append("\n");
    }
    
    prompt.append("\nReturn ONLY the exact city name from the list above, or 'NO_MATCH' if no suitable match is found.\n");
    prompt.append("Focus on finding the most accurate match based on the city name provided.");
    
    return prompt.toString();
  }

  /**
   * Create a prompt for GenAI to match country names
   */
  private String createCountryMatchingPrompt(String countryName, List<Country> countries) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Given the country name '").append(countryName).append("', ");
    prompt.append("find the BEST MATCHING country from the list below. ");
    prompt.append("Consider the following matching scenarios:\n\n");
    prompt.append("1. Exact name match\n");
    prompt.append("2. Alternative names or variations\n");
    prompt.append("3. Historical names or renames\n");
    prompt.append("4. Regional variations or local names\n");
    prompt.append("5. Abbreviations or short forms\n\n");
    prompt.append("Available countries:\n");
    
    for (int i = 0; i < countries.size(); i++) {
      Country country = countries.get(i);
      prompt.append(i + 1).append(". ").append(country.getName()).append("\n");
    }
    
    prompt.append("\nReturn ONLY the exact country name from the list above, or 'NO_MATCH' if no suitable match is found.\n");
    prompt.append("Focus on finding the most accurate match based on the country name provided.");
    
    return prompt.toString();
  }

  /**
   * Parse GenAI response for city matching
   */
  private City parseGenAICityResponse(String aiResponse, List<City> cities) {
    if (aiResponse == null || aiResponse.trim().isEmpty()) {
      return null;
    }
    
    // Try to find exact match from AI response
    String normalizedResponse = aiResponse.trim();
    if ("NO_MATCH".equalsIgnoreCase(normalizedResponse)) {
      return null; // No exact match found
    }
    
    for (City city : cities) {
      if (city.getName().equalsIgnoreCase(normalizedResponse)) {
        return city;
      }
    }
    
    // If no exact match, fallback to string similarity
    return findBestCityMatchBySimilarity(aiResponse, cities);
  }

  /**
   * Parse GenAI response for country matching
   */
  private Country parseGenAICountryResponse(String aiResponse, List<Country> countries) {
    if (aiResponse == null || aiResponse.trim().isEmpty()) {
      return null;
    }
    
    // Try to find exact match from AI response
    String normalizedResponse = aiResponse.trim();
    if ("NO_MATCH".equalsIgnoreCase(normalizedResponse)) {
      return null; // No exact match found
    }
    
    for (Country country : countries) {
      if (country.getName().equalsIgnoreCase(normalizedResponse)) {
        return country;
      }
    }
    
    // If no exact match, fallback to string similarity
    return findBestCountryMatchBySimilarity(aiResponse, countries);
  }

  /**
   * Store city mapping in similar content table
   */
  private void storeCityMapping(String parentName, String similarName, double confidence) {
    if (!similarContentRepository.existsByParentNameAndSimilarNameAndType(parentName, similarName, ContentType.CITY)) {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setParentName(parentName);
      similarContent.setSimilarName(similarName);
      similarContent.setType(ContentType.CITY);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setSource("AI_MAPPING");
      
      similarContentRepository.save(similarContent);
      log.info("Stored new city mapping: '{}' -> '{}' (confidence: {})", similarName, parentName, confidence);
    }
  }

  /**
   * Store country mapping in similar content table
   */
  private void storeCountryMapping(String parentName, String similarName, double confidence) {
    if (!similarContentRepository.existsByParentNameAndSimilarNameAndType(parentName, similarName, ContentType.COUNTRY)) {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setParentName(parentName);
      similarContent.setSimilarName(similarName);
      similarContent.setType(ContentType.COUNTRY);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setSource("AI_MAPPING");
      
      similarContentRepository.save(similarContent);
      log.info("Stored new country mapping: '{}' -> '{}' (confidence: {})", similarName, parentName, confidence);
    }
  }

  /**
   * Find top company matches using Apache Commons Text string similarity algorithms
   */
  private List<Company> findBestCompanyUsingStringSimilarity(String companyName) {
    try {
      // Get all companies from the dropdown service cache
      List<Company> allCompanies = dropdownService.getAllCompanies();
      if (allCompanies == null || allCompanies.isEmpty()) {
        return new ArrayList<>();
      }

      // Store all companies with their similarity scores
      List<CompanyScore> companyScores = new ArrayList<>();
      double threshold = 0.75; // High threshold for quality matches only

      for (Company company : allCompanies) {
        // Calculate similarity using multiple algorithms
        org.apache.commons.text.similarity.JaroWinklerSimilarity jaroWinkler = new org.apache.commons.text.similarity.JaroWinklerSimilarity();
        double jaroWinklerScore = jaroWinkler.apply(companyName.toLowerCase(), company.getName().toLowerCase());
        
        // Improved Levenshtein similarity calculation
        org.apache.commons.text.similarity.LevenshteinDistance levenshtein = new org.apache.commons.text.similarity.LevenshteinDistance();
        int levenshteinDistance = levenshtein.apply(companyName.toLowerCase(), company.getName().toLowerCase());
        int maxLength = Math.max(companyName.length(), company.getName().length());
        
        // Normalize Levenshtein distance to similarity score (0-1)
        double levenshteinScore = 0.0;
        if (maxLength > 0) {
          levenshteinScore = 1.0 - ((double) levenshteinDistance / maxLength);
        }
        
        // Add some debugging for the first few companies
        if (companyScores.size() < 3) {
          log.debug("Company: '{}' vs '{}' - JaroWinkler: {:.3f}, Levenshtein: {:.3f} (distance: {}, maxLength: {})", 
              companyName, company.getName(), jaroWinklerScore, levenshteinScore, levenshteinDistance, maxLength);
        }
        
        // Use weighted average of both algorithms for better results
        // Jaro-Winkler is better for company names, so give it more weight
        double weightedScore = (jaroWinklerScore * 0.7) + (levenshteinScore * 0.3);
        
        // Only include companies with high similarity scores (0.75+) for quality matches
        if (weightedScore >= threshold) {
          companyScores.add(new CompanyScore(company, weightedScore));
        }
      }

      // Sort by score in descending order and take top 3 for better accuracy
      companyScores.sort((a, b) -> Double.compare(b.score, a.score));
      
      // Log detailed scores for debugging
      log.info("String similarity analysis for '{}': Found {} companies above high threshold {}", 
          companyName, companyScores.size(), threshold);
      
      // Log top 3 scores for debugging
      for (int i = 0; i < Math.min(3, companyScores.size()); i++) {
        CompanyScore cs = companyScores.get(i);
        log.info("  {}. '{}' (score: {:.3f})", i + 1, cs.company.getName(), cs.score);
      }
      
      List<Company> topMatches = companyScores.stream()
          .limit(3)
          .map(cs -> cs.company)
          .collect(Collectors.toList());
      
      log.info("Returning top {} matches for GenAI validation", topMatches.size());
      
      return topMatches;
      
    } catch (Exception e) {
      log.error("Error in string similarity matching for company: {}", companyName, e);
      return new ArrayList<>();
    }
  }

  /**
   * Helper class to store company and its similarity score
   */
  private static class CompanyScore {
    final Company company;
    final double score;
    
    CompanyScore(Company company, double score) {
      this.company = company;
      this.score = score;
    }
  }

  /**
   * Validate company match with GenAI by presenting top similarity matches
   */
  private Company validateCompanyMatchWithGenAI(String inputCompanyName, List<Company> topMatches) {
    try {
      if (topMatches == null || topMatches.isEmpty()) {
        return null;
      }

      StringBuilder prompt = new StringBuilder();
      prompt.append("CRITICAL: You are matching company names. Be EXTREMELY strict and accurate.\n\n");
      prompt.append("Input company name: '").append(inputCompanyName).append("'\n\n");
      prompt.append("Available matches (ranked by similarity):\n");
      
      for (int i = 0; i < topMatches.size(); i++) {
        Company company = topMatches.get(i);
        prompt.append(i + 1).append(". ").append(company.getName()).append("\n");
      }
      
      prompt.append("\nSTRICT MATCHING RULES:\n");
      prompt.append("1. **EXACT MATCH**: Company names must be essentially the same\n");
      prompt.append("2. **SUBSIDIARY**: Only if clearly a subsidiary (e.g., 'Microsoft India' → 'Microsoft')\n");
      prompt.append("3. **ABBREVIATION**: Only official abbreviations (e.g., 'IBM' → 'International Business Machines')\n");
      prompt.append("4. **REJECT**: If names are different companies, even in same industry\n");
      prompt.append("5. **REJECT**: If names are similar but clearly different entities\n\n");
      prompt.append("EXAMPLES OF WHAT TO REJECT:\n");
      prompt.append("- 'Nala Robotics' vs 'Natobotics' → REJECT (different companies)\n");
      prompt.append("- 'Silicon Signals' vs 'Silicon Labs' → REJECT (different companies)\n");
      prompt.append("- 'Kobster.com' vs 'Kobie' → REJECT (different companies)\n\n");
      prompt.append("Respond with:\n");
      prompt.append("- The EXACT company name from the list above\n");
      prompt.append("- 'NO_MATCH' if none are suitable\n\n");
      prompt.append("Only respond with the exact company name or 'NO_MATCH', no additional text.");

      String aiResponse = callGenAIService(prompt.toString());
      
      if (aiResponse != null && !aiResponse.trim().equalsIgnoreCase("NO_MATCH")) {
        String selectedCompanyName = aiResponse.trim();
        
        // Find the selected company in our list
        for (Company company : topMatches) {
          if (company.getName().equalsIgnoreCase(selectedCompanyName)) {
            // Additional validation: Check if this match makes sense
            if (isValidCompanyMatch(inputCompanyName, company.getName())) {
              log.info("GenAI selected company match: '{}' -> '{}'", inputCompanyName, company.getName());
              return company;
            } else {
              log.warn("GenAI selected match rejected by validation: '{}' -> '{}'", inputCompanyName, company.getName());
              return null;
            }
          }
        }
        
        // If exact match not found, try partial matching with validation
        for (Company company : topMatches) {
          if (company.getName().toLowerCase().contains(selectedCompanyName.toLowerCase()) || 
              selectedCompanyName.toLowerCase().contains(company.getName().toLowerCase())) {
            if (isValidCompanyMatch(inputCompanyName, company.getName())) {
              log.info("GenAI selected company match (partial): '{}' -> '{}'", inputCompanyName, company.getName());
              return company;
            } else {
              log.warn("GenAI partial match rejected by validation: '{}' -> '{}'", inputCompanyName, company.getName());
              return null;
            }
          }
        }
        
        log.warn("GenAI response '{}' could not be matched to any company in the list", selectedCompanyName);
        return null;
      } else {
        log.info("GenAI found no suitable match for: '{}'", inputCompanyName);
        return null;
      }
      
    } catch (Exception e) {
      log.error("Error validating company matches with GenAI for: {}", inputCompanyName, e);
      return null;
    }
  }




}
