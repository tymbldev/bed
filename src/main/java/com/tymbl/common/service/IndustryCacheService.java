package com.tymbl.common.service;

import com.tymbl.common.repository.IndustryRepository;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class IndustryCacheService {

  private static final Logger logger = LoggerFactory.getLogger(IndustryCacheService.class);

  @Autowired
  private IndustryRepository industryRepository;

  // In-memory cache for industry statistics
  private final ConcurrentHashMap<String, List<Object[]>> industryStatsCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, List<Object[]>> topCompaniesCache = new ConcurrentHashMap<>();

  // Cache keys
  private static final String INDUSTRY_STATS_CACHE_KEY = "industry_stats";
  private static final String TOP_COMPANIES_CACHE_PREFIX = "top_companies_";


  /**
   * Get industry statistics with caching
   */
  @Cacheable(value = "industryStatistics", key = "#root.methodName")
  public List<Object[]> getCachedIndustryStatistics() {
    logger.debug("Fetching industry statistics from database (cache miss)");
    List<Object[]> stats = industryRepository.getIndustryStatistics();

    // Store in local cache as well
    industryStatsCache.put(INDUSTRY_STATS_CACHE_KEY, stats);

    return stats;
  }

  /**
   * Get top companies by industry with caching
   */
  @Cacheable(value = "topCompaniesByIndustry", key = "#industryId")
  public List<Object[]> getCachedTopCompaniesByIndustry(Long industryId) {
    logger.debug("Fetching top companies for industry {} from database (cache miss)", industryId);
    List<Object[]> companies = industryRepository.getTopCompaniesByIndustry(industryId);

    // Store in local cache as well
    topCompaniesCache.put(TOP_COMPANIES_CACHE_PREFIX + industryId, companies);

    return companies;
  }

  /**
   * Get industry statistics from local cache first, then database
   */
  public List<Object[]> getIndustryStatistics() {
    List<Object[]> cachedStats = industryStatsCache.get(INDUSTRY_STATS_CACHE_KEY);
    if (cachedStats != null) {
      logger.debug("Returning industry statistics from local cache");
      return cachedStats;
    }

    return getCachedIndustryStatistics();
  }

  /**
   * Get top companies by industry from local cache first, then database
   */
  public List<Object[]> getTopCompaniesByIndustry(Long industryId) {
    String cacheKey = TOP_COMPANIES_CACHE_PREFIX + industryId;
    List<Object[]> cachedCompanies = topCompaniesCache.get(cacheKey);
    if (cachedCompanies != null) {
      logger.debug("Returning top companies for industry {} from local cache", industryId);
      return cachedCompanies;
    }

    return getCachedTopCompaniesByIndustry(industryId);
  }


}
