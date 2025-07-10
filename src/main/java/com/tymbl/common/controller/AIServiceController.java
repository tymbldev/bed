package com.tymbl.common.controller;

import com.tymbl.common.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai-service")
@RequiredArgsConstructor
public class AIServiceController {

    private final AIService aiService;

    /**
     * Get the current status of AI services
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAIServiceStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", aiService.getAIServiceStatus());
        response.put("message", "AI Service Status Retrieved");
        response.put("timestamp", System.currentTimeMillis());
        
        log.info("AI Service status requested: {}", aiService.getAIServiceStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * Reset all circuit breakers
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetCircuitBreakers() {
        aiService.resetCircuitBreakers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", aiService.getAIServiceStatus());
        response.put("message", "Circuit breakers reset successfully");
        response.put("timestamp", System.currentTimeMillis());
        
        log.info("Circuit breakers reset requested. New status: {}", aiService.getAIServiceStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * Test AI service with a sample company
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testAIService(@RequestParam String companyName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<com.tymbl.jobs.entity.Company> result = aiService.generateCompanyInfo(companyName, "https://linkedin.com/company/test");
            
            response.put("success", result.isPresent());
            response.put("companyName", companyName);
            response.put("status", aiService.getAIServiceStatus());
            response.put("message", result.isPresent() ? "AI service test successful" : "AI service test failed");
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("AI Service test completed for company: {}. Success: {}", companyName, result.isPresent());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("companyName", companyName);
            response.put("status", aiService.getAIServiceStatus());
            response.put("message", "AI service test failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            log.error("AI Service test failed for company: {}", companyName, e);
            return ResponseEntity.ok(response);
        }
    }
} 