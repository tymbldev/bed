package com.tymbl.jobs.config;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for WebDriver setup with fallback mechanisms
 * This configuration is only active in non-test profiles to avoid conflicts
 * Provides multiple fallback options to ensure the application starts even if WebDriver fails
 */
@Configuration
@Profile("!test")
@Slf4j
public class WebDriverConfig {

    @Value("${webdriver.chrome.path:}")
    private String chromeDriverPath;

    @Value("${webdriver.firefox.path:}")
    private String firefoxDriverPath;

    @Value("${webdriver.timeout.seconds:30}")
    private int webDriverTimeoutSeconds;

    @Value("${webdriver.enabled:true}")
    private boolean webDriverEnabled;

    /**
     * Bean for WebDriver with multiple fallback options
     * Tries Chrome first, then Firefox, then returns null if both fail
     */
    @Bean
    public WebDriver webDriver() {
        if (!webDriverEnabled) {
            log.warn("🚫 WebDriver is disabled via configuration. Web crawling will be skipped.");
            return null;
        }

        log.info("🚀 Initializing WebDriver with fallback mechanisms...");

        // Try Chrome first
        WebDriver driver = tryChromeDriver();
        if (driver != null) {
            return driver;
        }

        // Try Firefox as fallback
        driver = tryFirefoxDriver();
        if (driver != null) {
            return driver;
        }

        // If both fail, log warning and return null
        log.warn("⚠️ All WebDriver initialization attempts failed. Web crawling will be disabled. " +
                "Application will continue to work without web crawling functionality.");
        return null;
    }

    /**
     * Try to initialize Chrome WebDriver
     */
    private WebDriver tryChromeDriver() {
        try {
            log.info("🔄 Attempting to initialize Chrome WebDriver...");
            
            ChromeOptions options = createChromeOptions();
            
            // Set ChromeDriver path if provided
            if (chromeDriverPath != null && !chromeDriverPath.trim().isEmpty()) {
                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
                log.info("📍 Using ChromeDriver path: {}", chromeDriverPath);
            }

            WebDriver driver = new ChromeDriver(options);
            configureDriverTimeouts(driver);
            
            // Test the driver with a simple operation
            testWebDriver(driver, "Chrome");
            
            log.info("✅ Chrome WebDriver initialized successfully");
            return driver;
            
        } catch (Exception e) {
            log.warn("❌ Failed to initialize Chrome WebDriver: {}", e.getMessage());
            log.debug("Chrome WebDriver initialization error details:", e);
            return null;
        }
    }

    /**
     * Try to initialize Firefox WebDriver as fallback
     */
    private WebDriver tryFirefoxDriver() {
        try {
            log.info("🔄 Attempting to initialize Firefox WebDriver as fallback...");
            
            FirefoxOptions options = createFirefoxOptions();
            
            // Set FirefoxDriver path if provided
            if (firefoxDriverPath != null && !firefoxDriverPath.trim().isEmpty()) {
                System.setProperty("webdriver.gecko.driver", firefoxDriverPath);
                log.info("📍 Using FirefoxDriver path: {}", firefoxDriverPath);
            }

            WebDriver driver = new FirefoxDriver(options);
            configureDriverTimeouts(driver);
            
            // Test the driver with a simple operation
            testWebDriver(driver, "Firefox");
            
            log.info("✅ Firefox WebDriver initialized successfully as fallback");
            return driver;
            
        } catch (Exception e) {
            log.warn("❌ Failed to initialize Firefox WebDriver: {}", e.getMessage());
            log.debug("Firefox WebDriver initialization error details:", e);
            return null;
        }
    }

    /**
     * Create Chrome options with optimized settings
     */
    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        
        // Add headless mode for server environments
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        
        // Disable images and CSS for faster loading
        options.addArguments("--disable-images");
        options.addArguments("--disable-javascript");
        
        // Additional performance optimizations
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=VizDisplayCompositor");
        
        // Additional stability options
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-features=TranslateUI");
        options.addArguments("--disable-ipc-flooding-protection");
        
        // Memory optimization
        options.addArguments("--memory-pressure-off");
        options.addArguments("--max_old_space_size=4096");
        
        return options;
    }

    /**
     * Create Firefox options with optimized settings
     */
    private FirefoxOptions createFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        
        // Add headless mode
        options.addArguments("--headless");
        
        // Performance optimizations
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("media.volume_scale", "0.0");
        options.addPreference("dom.push.enabled", false);
        options.addPreference("geo.enabled", false);
        options.addPreference("browser.cache.disk.enable", false);
        options.addPreference("browser.cache.memory.enable", false);
        options.addPreference("browser.cache.offline.enable", false);
        options.addPreference("network.http.use-cache", false);
        
        return options;
    }

    /**
     * Configure driver timeouts
     */
    private void configureDriverTimeouts(WebDriver driver) {
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(webDriverTimeoutSeconds));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().setScriptTimeout(Duration.ofSeconds(30));
    }

    /**
     * Test WebDriver with a simple operation
     */
    private void testWebDriver(WebDriver driver, String driverType) {
        try {
            log.debug("🧪 Testing {} WebDriver...", driverType);
            
            // Test with a simple data URL
            driver.get("data:text/html,<html><body><h1>WebDriver Test</h1></body></html>");
            
            // Verify we can get the page source
            String pageSource = driver.getPageSource();
            if (pageSource == null || pageSource.trim().isEmpty()) {
                throw new RuntimeException("WebDriver test failed: Empty page source");
            }
            
            log.debug("✅ {} WebDriver test passed", driverType);
            
        } catch (Exception e) {
            log.error("❌ {} WebDriver test failed: {}", driverType, e.getMessage());
            throw new RuntimeException("WebDriver test failed for " + driverType, e);
        }
    }
}
