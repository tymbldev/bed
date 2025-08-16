package com.tymbl.common.service;

import com.tymbl.common.repository.IndustryRepository;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndustryCacheService {

  private final IndustryRepository industryRepository;

  // In-memory cache for industry statistics
  private final ConcurrentMap<String, List<Object[]>> industryStatsCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, List<Object[]>> topCompaniesCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

  // Cache keys
  private static final String INDUSTRY_STATS_CACHE_KEY = "industry_stats";
  private static final String TOP_COMPANIES_CACHE_PREFIX = "top_companies_";
  
  // Cache TTL in milliseconds (30 minutes)
  private static final long CACHE_TTL = 30 * 60 * 1000L;

  /**
   * Check if cache entry is expired
   */
  private boolean isCacheExpired(String cacheKey) {
    Long timestamp = cacheTimestamps.get(cacheKey);
    return timestamp == null || (System.currentTimeMillis() - timestamp) > CACHE_TTL;
  }

  /**
   * Get industry statistics with caching
   */
  public List<Object[]> getCachedIndustryStatistics() {
    log.debug("Fetching industry statistics from database (cache miss)");
    List<Object[]> stats = industryRepository.getIndustryStatistics();

    // Store in local cache
    industryStatsCache.put(INDUSTRY_STATS_CACHE_KEY, stats);
    cacheTimestamps.put(INDUSTRY_STATS_CACHE_KEY, System.currentTimeMillis());

    return stats;
  }

  /**
   * Get top companies by industry with caching
   */
  public List<Object[]> getCachedTopCompaniesByIndustry(Long industryId) {
    log.debug("Fetching top companies for industry {} from database (cache miss)", industryId);
    List<Object[]> companies = industryRepository.getTopCompaniesByIndustry(industryId);

    // Store in local cache
    String cacheKey = TOP_COMPANIES_CACHE_PREFIX + industryId;
    topCompaniesCache.put(cacheKey, companies);
    cacheTimestamps.put(cacheKey, System.currentTimeMillis());

    return companies;
  }

  /**
   * Get industry statistics from local cache first, then database
   */
  public List<Object[]> getIndustryStatistics() {
    // Check cache first
    if (!isCacheExpired(INDUSTRY_STATS_CACHE_KEY)) {
      List<Object[]> cachedStats = industryStatsCache.get(INDUSTRY_STATS_CACHE_KEY);
      if (cachedStats != null) {
        log.debug("Returning industry statistics from local cache");
        return cachedStats;
      }
    }

    return getCachedIndustryStatistics();
  }

  /**
   * Get top companies by industry from local cache first, then database
   */
  public List<Object[]> getTopCompaniesByIndustry(Long industryId) {
    String cacheKey = TOP_COMPANIES_CACHE_PREFIX + industryId;
    
    // Check cache first
    if (!isCacheExpired(cacheKey)) {
      List<Object[]> cachedCompanies = topCompaniesCache.get(cacheKey);
      if (cachedCompanies != null) {
        log.debug("Returning top companies for industry {} from local cache", industryId);
        return cachedCompanies;
      }
    }

    return getCachedTopCompaniesByIndustry(industryId);
  }

  /**
   * Clear all caches
   */
  public void clearAllCaches() {
    industryStatsCache.clear();
    topCompaniesCache.clear();
    cacheTimestamps.clear();
    log.info("All industry caches cleared");
  }

  /**
   * Clear specific cache
   */
  public void clearCache(String cacheKey) {
    if (INDUSTRY_STATS_CACHE_KEY.equals(cacheKey)) {
      industryStatsCache.remove(cacheKey);
      cacheTimestamps.remove(cacheKey);
      log.info("Industry statistics cache cleared");
    } else if (cacheKey.startsWith(TOP_COMPANIES_CACHE_PREFIX)) {
      topCompaniesCache.remove(cacheKey);
      cacheTimestamps.remove(cacheKey);
      log.info("Top companies cache cleared for key: {}", cacheKey);
    }
  }
}
