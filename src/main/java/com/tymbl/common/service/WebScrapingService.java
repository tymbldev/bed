package com.tymbl.common.service;

import com.tymbl.common.entity.UrlContent;
import com.tymbl.common.repository.UrlContentRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebScrapingService {

  private final UrlContentRepository urlContentRepository;

  private static final int TIMEOUT_SECONDS = 30;
  private static final int PAGE_LOAD_TIMEOUT = 60;

  /**
   * Extract text content from a URL using Selenium WebDriver
   *
   * @param url The URL to scrape
   * @return Optional containing the extracted text
   */
  public Optional<String> extractTextFromUrl(String url) {
    log.info("Starting text extraction from URL: {}", url);

    // Check if we already have this URL cached
    Optional<UrlContent> existingContent = urlContentRepository.findByUrl(url);
    if (existingContent.isPresent()) {
      UrlContent content = existingContent.get();
      if ("SUCCESS".equals(content.getExtractionStatus()) && content.getExtractedText() != null) {
        log.info("Using cached content for URL: {}", url);
        return Optional.of(content.getExtractedText());
      }
    }

    WebDriver driver = null;
    try {
      // Setup Chrome options
      ChromeOptions options = new ChromeOptions();
      options.addArguments("--headless"); // Run in headless mode
      options.addArguments("--no-sandbox");
      options.addArguments("--disable-dev-shm-usage");
      options.addArguments("--disable-gpu");
      options.addArguments("--window-size=1920,1080");
      options.addArguments(
          "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

      // Initialize WebDriver
      driver = new ChromeDriver(options);
      driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT));
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

      log.info("Navigating to URL: {}", url);
      driver.get(url);

      // Wait for page to load
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
      wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

      // Scroll to load dynamic content
      scrollPage(driver);

      // Extract text content
      String extractedText = extractTextContent(driver);

      if (extractedText != null && !extractedText.trim().isEmpty()) {
        // Save to database
        saveUrlContent(url, extractedText, "SUCCESS", null);
        log.info("Successfully extracted text from URL: {} (length: {})", url,
            extractedText.length());
        return Optional.of(extractedText);
      } else {
        log.warn("No text content extracted from URL: {}", url);
        saveUrlContent(url, null, "FAILED", "No text content found");
        return Optional.empty();
      }

    } catch (Exception e) {
      log.error("Error extracting text from URL: {}", url, e);
      saveUrlContent(url, null, "FAILED", e.getMessage());
      return Optional.empty();
    } finally {
      if (driver != null) {
        try {
          driver.quit();
        } catch (Exception e) {
          log.warn("Error closing WebDriver", e);
        }
      }
    }
  }

  /**
   * Scroll the page to load dynamic content
   */
  private void scrollPage(WebDriver driver) {
    try {
      JavascriptExecutor js = (JavascriptExecutor) driver;

      // Scroll to bottom
      js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
      Thread.sleep(2000);

      // Scroll to top
      js.executeScript("window.scrollTo(0, 0);");
      Thread.sleep(1000);

      // Scroll to middle
      js.executeScript("window.scrollTo(0, document.body.scrollHeight/2);");
      Thread.sleep(1000);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Interrupted during page scrolling", e);
    } catch (Exception e) {
      log.warn("Error during page scrolling", e);
    }
  }

  /**
   * Extract text content from the page
   */
  private String extractTextContent(WebDriver driver) {
    try {
      // Try to find main content areas first
      String[] contentSelectors = {
          "main", "article", ".content", ".main-content", ".job-description",
          ".job-details", ".description", ".posting-content", ".job-content"
      };

      for (String selector : contentSelectors) {
        try {
          WebElement element = driver.findElement(By.cssSelector(selector));
          if (element != null && !element.getText().trim().isEmpty()) {
            return element.getText();
          }
        } catch (Exception e) {
          // Continue to next selector
        }
      }

      // Fallback to body text
      WebElement body = driver.findElement(By.tagName("body"));
      return body.getText();

    } catch (Exception e) {
      log.error("Error extracting text content", e);
      return null;
    }
  }

  /**
   * Save URL content to database
   */
  private void saveUrlContent(String url, String extractedText, String status,
      String errorMessage) {
    try {
      UrlContent urlContent = urlContentRepository.findByUrl(url)
          .orElse(new UrlContent());

      urlContent.setUrl(url);
      urlContent.setExtractedText(extractedText);
      urlContent.setExtractionStatus(status);
      urlContent.setErrorMessage(errorMessage);
      urlContent.setExtractedAt(LocalDateTime.now());

      urlContentRepository.save(urlContent);
      log.info("Saved URL content for: {} with status: {}", url, status);

    } catch (Exception e) {
      log.error("Error saving URL content for: {}", url, e);
    }
  }
} 