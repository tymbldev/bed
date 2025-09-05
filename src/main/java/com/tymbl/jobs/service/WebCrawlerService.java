package com.tymbl.jobs.service;

import com.tymbl.jobs.entity.ExternalJobDetailsFromCompanyPortal;
import com.tymbl.jobs.repository.ExternalJobDetailsFromCompanyPortalRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebCrawlerService {

  @Autowired
  private ExternalJobDetailsFromCompanyPortalRepository crawledContentRepository;

  @Autowired(required = false)
  private WebDriver webDriver;

  @Value("${webcrawler.fallback.enabled:true}")
  private boolean fallbackEnabled;

  private static final int DEFAULT_TIMEOUT_SECONDS = 30;
  private static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 60;

  /**
   * Crawl a job URL and extract content
   */
  @Transactional
  public ExternalJobDetailsFromCompanyPortal crawlJobUrl(Long externalJobDetailId, String redirectUrl) {
    long startTime = System.currentTimeMillis();
    
    log.info("🕷️ Starting web crawl for external job ID: {} with URL: {}", externalJobDetailId, redirectUrl);

    // Check if already crawled successfully
    java.util.Optional<ExternalJobDetailsFromCompanyPortal> existingCrawl = crawledContentRepository.findByExternalJobDetailIdAndCrawlStatus(
        externalJobDetailId, "SUCCESS");
    if (existingCrawl.isPresent()) {
      log.info("✅ Content already crawled successfully for external job ID: {}", externalJobDetailId);
      return existingCrawl.get();
    }

    // Create new crawl record
    ExternalJobDetailsFromCompanyPortal crawlRecord = ExternalJobDetailsFromCompanyPortal.builder()
        .externalJobDetailId(externalJobDetailId)
        .redirectUrl(redirectUrl)
        .crawlStatus("PENDING")
        .createdAt(LocalDateTime.now())
        .build();

    // Check if WebDriver is available
    if (webDriver == null) {
      log.warn("⚠️ WebDriver is not available. Attempting fallback crawling for external job ID: {}", externalJobDetailId);
      return handleWebDriverUnavailable(externalJobDetailId, redirectUrl, crawlRecord, startTime);
    }

    WebDriver driver = null;
    try {
      // Use the injected WebDriver or create a new one
      driver = webDriver;
      
      // Navigate to the URL
      log.info("🌐 Navigating to URL: {}", redirectUrl);
      driver.get(redirectUrl);
      
      // Wait for page to load
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
      wait.until(webDriver -> webDriver.getPageSource() != null && !webDriver.getPageSource().isEmpty());
      
      // Get raw HTML content
      String rawHtmlContent = driver.getPageSource();
      log.info("📄 Retrieved HTML content, size: {} characters", rawHtmlContent.length());
      
      // Parse HTML and extract text content
      String parsedTextContent = extractTextFromHtml(rawHtmlContent);
      log.info("📝 Extracted text content, size: {} characters", parsedTextContent.length());
      
      // Update crawl record with success
      long crawlDuration = System.currentTimeMillis() - startTime;
      crawlRecord.setRawHtmlContent(rawHtmlContent);
      crawlRecord.setParsedTextContent(parsedTextContent);
      crawlRecord.setCrawlStatus("SUCCESS");
      crawlRecord.setCrawlDurationMs(crawlDuration);
      crawlRecord.setUpdatedAt();
      
      // Save to database
      ExternalJobDetailsFromCompanyPortal savedRecord = crawledContentRepository.save(crawlRecord);
      
      log.info("✅ Successfully crawled and saved content for external job ID: {} in {}ms", 
          externalJobDetailId, crawlDuration);
      
      return savedRecord;
      
    } catch (Exception e) {
      long crawlDuration = System.currentTimeMillis() - startTime;
      log.error("❌ Failed to crawl URL for external job ID {}: {}", externalJobDetailId, e.getMessage(), e);
      
      // Try fallback if enabled
      if (fallbackEnabled) {
        log.info("🔄 Attempting fallback crawling for external job ID: {}", externalJobDetailId);
        return handleWebDriverFailure(externalJobDetailId, redirectUrl, crawlRecord, startTime, e);
      }
      
      // Update crawl record with failure
      crawlRecord.setCrawlStatus("FAILED");
      crawlRecord.setErrorMessage(e.getMessage());
      crawlRecord.setCrawlDurationMs(crawlDuration);
      crawlRecord.setUpdatedAt();
      
      // Save failed record
      crawledContentRepository.save(crawlRecord);
      
      throw new RuntimeException("Failed to crawl job URL: " + e.getMessage(), e);
      
    } finally {
      // Note: We don't quit the injected WebDriver as it's managed by Spring
      if (driver != null && driver != webDriver) {
        try {
          driver.quit();
          log.debug("🧹 WebDriver cleaned up successfully");
        } catch (Exception e) {
          log.warn("⚠️ Error cleaning up WebDriver: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * Handle case when WebDriver is not available
   */
  private ExternalJobDetailsFromCompanyPortal handleWebDriverUnavailable(Long externalJobDetailId, String redirectUrl, 
      ExternalJobDetailsFromCompanyPortal crawlRecord, long startTime) {
    
    log.info("🔄 WebDriver unavailable, attempting HTTP fallback for external job ID: {}", externalJobDetailId);
    
    try {
      // Try HTTP fallback using JSoup
      String htmlContent = fetchContentWithHttpFallback(redirectUrl);
      
      if (htmlContent != null && !htmlContent.trim().isEmpty()) {
        // Parse HTML and extract text content
        String parsedTextContent = extractTextFromHtml(htmlContent);
        
        // Update crawl record with success
        long crawlDuration = System.currentTimeMillis() - startTime;
        crawlRecord.setRawHtmlContent(htmlContent);
        crawlRecord.setParsedTextContent(parsedTextContent);
        crawlRecord.setCrawlStatus("SUCCESS");
        crawlRecord.setCrawlDurationMs(crawlDuration);
        crawlRecord.setUpdatedAt();
        
        // Save to database
        ExternalJobDetailsFromCompanyPortal savedRecord = crawledContentRepository.save(crawlRecord);
        
        log.info("✅ Successfully crawled content using HTTP fallback for external job ID: {} in {}ms", 
            externalJobDetailId, crawlDuration);
        
        return savedRecord;
      } else {
        throw new RuntimeException("HTTP fallback returned empty content");
      }
      
    } catch (Exception e) {
      long crawlDuration = System.currentTimeMillis() - startTime;
      log.error("❌ HTTP fallback also failed for external job ID {}: {}", externalJobDetailId, e.getMessage());
      
      // Update crawl record with failure
      crawlRecord.setCrawlStatus("FAILED");
      crawlRecord.setErrorMessage("WebDriver unavailable and HTTP fallback failed: " + e.getMessage());
      crawlRecord.setCrawlDurationMs(crawlDuration);
      crawlRecord.setUpdatedAt();
      
      // Save failed record
      crawledContentRepository.save(crawlRecord);
      
      // Don't throw exception, return the failed record
      return crawlRecord;
    }
  }

  /**
   * Handle WebDriver failure with fallback
   */
  private ExternalJobDetailsFromCompanyPortal handleWebDriverFailure(Long externalJobDetailId, String redirectUrl, 
      ExternalJobDetailsFromCompanyPortal crawlRecord, long startTime, Exception originalException) {
    
    log.info("🔄 WebDriver failed, attempting HTTP fallback for external job ID: {}", externalJobDetailId);
    
    try {
      // Try HTTP fallback using JSoup
      String htmlContent = fetchContentWithHttpFallback(redirectUrl);
      
      if (htmlContent != null && !htmlContent.trim().isEmpty()) {
        // Parse HTML and extract text content
        String parsedTextContent = extractTextFromHtml(htmlContent);
        
        // Update crawl record with success
        long crawlDuration = System.currentTimeMillis() - startTime;
        crawlRecord.setRawHtmlContent(htmlContent);
        crawlRecord.setParsedTextContent(parsedTextContent);
        crawlRecord.setCrawlStatus("SUCCESS");
        crawlRecord.setCrawlDurationMs(crawlDuration);
        crawlRecord.setUpdatedAt();
        
        // Save to database
        ExternalJobDetailsFromCompanyPortal savedRecord = crawledContentRepository.save(crawlRecord);
        
        log.info("✅ Successfully crawled content using HTTP fallback after WebDriver failure for external job ID: {} in {}ms", 
            externalJobDetailId, crawlDuration);
        
        return savedRecord;
      } else {
        throw new RuntimeException("HTTP fallback returned empty content");
      }
      
    } catch (Exception e) {
      long crawlDuration = System.currentTimeMillis() - startTime;
      log.error("❌ HTTP fallback also failed for external job ID {}: {}", externalJobDetailId, e.getMessage());
      
      // Update crawl record with failure
      crawlRecord.setCrawlStatus("FAILED");
      crawlRecord.setErrorMessage("WebDriver failed: " + originalException.getMessage() + 
          "; HTTP fallback failed: " + e.getMessage());
      crawlRecord.setCrawlDurationMs(crawlDuration);
      crawlRecord.setUpdatedAt();
      
      // Save failed record
      crawledContentRepository.save(crawlRecord);
      
      // Don't throw exception, return the failed record
      return crawlRecord;
    }
  }

  /**
   * Fetch content using HTTP fallback (JSoup)
   */
  private String fetchContentWithHttpFallback(String url) {
    try {
      log.debug("🌐 Attempting HTTP fallback for URL: {}", url);
      
      // Use JSoup to fetch content directly
      Document doc = org.jsoup.Jsoup.connect(url)
          .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
          .timeout(30000) // 30 seconds timeout
          .followRedirects(true)
          .get();
      
      String htmlContent = doc.html();
      log.debug("📄 HTTP fallback retrieved content, size: {} characters", htmlContent.length());
      
      return htmlContent;
      
    } catch (Exception e) {
      log.error("❌ HTTP fallback failed for URL {}: {}", url, e.getMessage());
      throw new RuntimeException("HTTP fallback failed: " + e.getMessage(), e);
    }
  }

  /**
   * Extract clean text content from HTML using JSoup
   */
  private String extractTextFromHtml(String htmlContent) {
    try {
      Document doc = Jsoup.parse(htmlContent);
      
      // Remove script and style elements
      doc.select("script").remove();
      doc.select("style").remove();
      doc.select("noscript").remove();
      
      // Get text content
      String text = doc.text();
      
      // Clean up the text
      text = text.replaceAll("\\s+", " "); // Replace multiple whitespaces with single space
      text = text.trim();
      
      return text;
      
    } catch (Exception e) {
      log.error("❌ Error parsing HTML content: {}", e.getMessage(), e);
      return "Error parsing HTML content: " + e.getMessage();
    }
  }

  /**
   * Get crawled content by external job detail ID
   */
  public ExternalJobDetailsFromCompanyPortal getCrawledContent(Long externalJobDetailId) {
    return crawledContentRepository.findByExternalJobDetailId(externalJobDetailId).orElse(null);
  }

  /**
   * Check if content has been crawled successfully
   */
  public boolean isContentCrawled(Long externalJobDetailId) {
    return crawledContentRepository.findByExternalJobDetailIdAndCrawlStatus(
        externalJobDetailId, "SUCCESS").isPresent();
  }

  /**
   * Get crawl statistics
   */
  public CrawlStatistics getCrawlStatistics() {
    long totalCrawls = crawledContentRepository.count();
    long successfulCrawls = crawledContentRepository.countByCrawlStatus("SUCCESS");
    long failedCrawls = crawledContentRepository.countByCrawlStatus("FAILED");
    long pendingCrawls = crawledContentRepository.countByCrawlStatus("PENDING");
    
    return CrawlStatistics.builder()
        .totalCrawls(totalCrawls)
        .successfulCrawls(successfulCrawls)
        .failedCrawls(failedCrawls)
        .pendingCrawls(pendingCrawls)
        .successRate(totalCrawls > 0 ? (double) successfulCrawls / totalCrawls * 100 : 0.0)
        .build();
  }

  /**
   * Statistics class for crawl metrics
   */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class CrawlStatistics {
    private long totalCrawls;
    private long successfulCrawls;
    private long failedCrawls;
    private long pendingCrawls;
    private double successRate;
  }
}
