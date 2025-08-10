package com.tymbl.common.service;

import com.tymbl.common.dto.CompanyGenerationResponse;
import com.tymbl.jobs.entity.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final GeminiService geminiService;

    // Circuit breaker flags
    private final AtomicBoolean geminiDisabled = new AtomicBoolean(false);
    private final AtomicBoolean crawlingDisabled = new AtomicBoolean(false);

    // Failure counters
    private final AtomicInteger geminiFailureCount = new AtomicInteger(0);

    @Value("${ai.circuitbreaker.gemini.failure-threshold:1}")
    private int geminiFailureThreshold;

    /**
     * Generate company information using Gemini AI.
     *
     * @param companyName The name of the company
     * @return Optional<Company> containing the generated company information
     */
    public Optional<Company> generateCompanyInfo(String companyName) {
        log.info("Starting AI-powered company information generation for: {}", companyName);

        // Check if crawling is disabled
        if (crawlingDisabled.get()) {
            log.warn("AI crawling is disabled due to previous failures. Skipping company: {}", companyName);
            return Optional.empty();
        }

        // Try Gemini (unless disabled or threshold is 0)
        if (!geminiDisabled.get() && geminiFailureThreshold > 0) {
            try {
                log.info("Attempting to generate company info using Gemini AI for: {}", companyName);
                Optional<Company> geminiResult = geminiService.generateCompanyInfo(companyName);

                if (geminiResult.isPresent()) {
                    log.info("Successfully generated company info using Gemini AI for: {}", companyName);
                    geminiFailureCount.set(0); // reset on success
                    return geminiResult;
                } else {
                    log.warn("Gemini AI returned empty result for company: {}", companyName);
                    incrementGeminiFailure();
                }
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("Rate limit exceeded")) {
                    log.error("Gemini AI hit rate limit for company: {}", companyName);
                } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                    log.error("Gemini AI request timed out for company: {}", companyName);
                } else {
                    log.error("Unexpected error with Gemini AI for company: {}", companyName, e);
                }
                incrementGeminiFailure();
            } catch (Exception e) {
                log.error("Error with Gemini AI for company: {}", companyName, e);
                incrementGeminiFailure();
            }
        } else if (geminiFailureThreshold == 0) {
            log.warn("Gemini AI is disabled via configuration (failure threshold = 0)");
            geminiDisabled.set(true);
        }

        log.error("Failed to generate company info using Gemini AI for company: {}", companyName);
        return Optional.empty();
    }
    
    /**
     * Generate company information using Gemini AI.
     *
     * @param companyName The name of the company
     * @return CompanyGenerationResponse containing the generated company information
     */
    public CompanyGenerationResponse generateCompanyInfoWithJunkDetection(String companyName) {
        log.info("Starting AI-powered company information generation for: {}", companyName);

        // Check if crawling is disabled
        if (crawlingDisabled.get()) {
            log.warn("AI crawling is disabled due to previous failures. Skipping company: {}", companyName);
            return CompanyGenerationResponse.builder()
                .success(false)
                .errorMessage("AI crawling is disabled due to previous failures")
                .build();
        }

        // Try Gemini (unless disabled or threshold is 0)
        if (!geminiDisabled.get() && geminiFailureThreshold > 0) {
            try {
                log.info("Attempting to generate company info using Gemini AI for: {}", companyName);
                CompanyGenerationResponse geminiResult = geminiService.generateCompanyInfoWithJunkDetection(companyName);

                if (geminiResult.isSuccess()) {
                    log.info("Successfully generated company info using Gemini AI for: {}", companyName);
                    geminiFailureCount.set(0); // reset on success
                    return geminiResult;
                } else {
                    log.warn("Gemini AI returned unsuccessful result for company: {}", companyName);
                    incrementGeminiFailure();
                }
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("Rate limit exceeded")) {
                    log.error("Gemini AI hit rate limit for company: {}", companyName);
                } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                    log.error("Gemini AI request timed out for company: {}", companyName);
                } else {
                    log.error("Unexpected error with Gemini AI for company: {}", companyName, e);
                }
                incrementGeminiFailure();
            } catch (Exception e) {
                log.error("Error with Gemini AI for company: {}", companyName, e);
                incrementGeminiFailure();
            }
        } else if (geminiFailureThreshold == 0) {
            log.warn("Gemini AI is disabled via configuration (failure threshold = 0)");
            geminiDisabled.set(true);
        }

        log.error("Failed to generate company info using Gemini AI for company: {}", companyName);
        return CompanyGenerationResponse.builder()
            .success(false)
            .errorMessage("Failed to generate company info using Gemini AI")
            .build();
    }

    private void incrementGeminiFailure() {
        int count = geminiFailureCount.incrementAndGet();
        log.warn("Gemini AI failure count: {}/{}", count, geminiFailureThreshold);
        if (count >= geminiFailureThreshold) {
            disableGemini();
        }
    }

    /**
     * Disable Gemini AI service due to errors
     */
    private void disableGemini() {
        if (geminiDisabled.compareAndSet(false, true)) {
            log.error("DISABLING Gemini AI service due to errors. Disabling crawling entirely.");
            crawlingDisabled.set(true);
        }
    }

    /**
     * Reset all circuit breakers (for testing or manual recovery)
     */
    public void resetCircuitBreakers() {
        geminiDisabled.set(false);
        crawlingDisabled.set(false);
        geminiFailureCount.set(0);
        log.info("Circuit breakers reset. Gemini AI service enabled.");
    }

    /**
     * Get the current status of AI services
     */
    public String getAIServiceStatus() {
        return String.format("Gemini: %s, Crawling: %s", 
            geminiDisabled.get() ? "DISABLED" : "ENABLED",
            crawlingDisabled.get() ? "DISABLED" : "ENABLED");
    }
}