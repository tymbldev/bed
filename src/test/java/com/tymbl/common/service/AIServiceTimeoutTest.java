package com.tymbl.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceTimeoutTest {

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private AIService aiService;

    @Test
    void testGeminiTimeout() {
        // Given
        String companyName = "Test Company";
        String linkedinUrl = "https://linkedin.com/company/test";
        
        // Mock Gemini service to throw timeout exception
        when(geminiService.generateCompanyInfo(companyName, linkedinUrl))
            .thenThrow(new RuntimeException("Request timeout for Gemini API"));
        
        // When
        Optional<com.tymbl.jobs.entity.Company> result = aiService.generateCompanyInfo(companyName, linkedinUrl);
        
        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGeminiRateLimit() {
        // Given
        String companyName = "Test Company";
        String linkedinUrl = "https://linkedin.com/company/test";
        
        // Mock Gemini service to throw rate limit exception
        when(geminiService.generateCompanyInfo(companyName, linkedinUrl))
            .thenThrow(new RuntimeException("Rate limit exceeded for Gemini API"));
        
        // When
        Optional<com.tymbl.jobs.entity.Company> result = aiService.generateCompanyInfo(companyName, linkedinUrl);
        
        // Then
        assertFalse(result.isPresent());
    }
} 