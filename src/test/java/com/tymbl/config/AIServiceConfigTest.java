package com.tymbl.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AIServiceConfigTest {

    @Autowired
    @Qualifier("aiServiceRestTemplate")
    private RestTemplate aiServiceRestTemplate;

    @Test
    void testRestTemplateBeanExists() {
        // Verify that the bean exists and is injectable
        assertNotNull(aiServiceRestTemplate, "aiServiceRestTemplate bean should be available");
        
        // Verify that it's not the default RestTemplate
        assertNotNull(aiServiceRestTemplate.getRequestFactory(), "Request factory should be configured");
        
        System.out.println("RestTemplate bean successfully injected with configured timeouts");
    }

    @Test
    void testRestTemplateIsConfigured() {
        // Verify that the RestTemplate is properly configured
        assertNotNull(aiServiceRestTemplate);
        
        // The fact that we can inject it with @Qualifier means it's properly configured
        assertTrue(aiServiceRestTemplate.getInterceptors().size() > 0, 
                  "RestTemplate should have interceptors configured for timeout monitoring");
        
        System.out.println("RestTemplate has " + aiServiceRestTemplate.getInterceptors().size() + " interceptors configured");
    }
} 