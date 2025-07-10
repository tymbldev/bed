package com.tymbl.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceCircuitBreakerTest {

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private AIService aiService;

    @BeforeEach
    void setUp() {
        // Reset circuit breakers before each test
        aiService.resetCircuitBreakers();
    }

    @Test
    void testGeminiFailureDisablesGemini() {
        // Given
        String companyName = "Test Company";
        String linkedinUrl = "https://linkedin.com/company/test";
        
        // Mock Gemini to throw exception
        when(geminiService.generateCompanyInfo(companyName, linkedinUrl))
            .thenThrow(new RuntimeException("Gemini API error"));
        
        // When - First call should fail Gemini and disable it
        Optional<com.tymbl.jobs.entity.Company> result1 = aiService.generateCompanyInfo(companyName, linkedinUrl);
        
        // Then - Should get empty result and Gemini should be disabled
        assertFalse(result1.isPresent());
        assertEquals("Gemini: DISABLED, Crawling: DISABLED", aiService.getAIServiceStatus());
        
        // When - Second call should skip Gemini
        Optional<com.tymbl.jobs.entity.Company> result2 = aiService.generateCompanyInfo(companyName, linkedinUrl);
        
        // Then - Should still get empty result
        assertFalse(result2.isPresent());
    }

    @Test
    void testResetCircuitBreakers() {
        // Given - Disable all services
        aiService.resetCircuitBreakers();
        assertEquals("Gemini: ENABLED, Crawling: ENABLED", aiService.getAIServiceStatus());
        
        // When - Reset circuit breakers
        aiService.resetCircuitBreakers();
        
        // Then - All services should be enabled
        assertEquals("Gemini: ENABLED, Crawling: ENABLED", aiService.getAIServiceStatus());
    }

    @Test
    void testCrawlingDisabledSkipsAllServices() {
        // Given
        String companyName = "Test Company";
        String linkedinUrl = "https://linkedin.com/company/test";
        
        // First, trigger the circuit breaker to disable crawling
        when(geminiService.generateCompanyInfo(companyName, linkedinUrl))
            .thenThrow(new RuntimeException("Gemini API error"));
        
        // This should disable Gemini and crawling
        aiService.generateCompanyInfo(companyName, linkedinUrl);
        
        // Verify crawling is disabled
        assertEquals("Gemini: DISABLED, Crawling: DISABLED", aiService.getAIServiceStatus());
        
        // Mock Gemini to return success (but it shouldn't be called)
        when(geminiService.generateCompanyInfo(anyString(), anyString()))
            .thenReturn(Optional.of(new com.tymbl.jobs.entity.Company()));
        
        // When - Call with crawling disabled
        Optional<com.tymbl.jobs.entity.Company> result = aiService.generateCompanyInfo(companyName, linkedinUrl);
        
        // Then - Should get empty result immediately
        assertFalse(result.isPresent());
    }
} 