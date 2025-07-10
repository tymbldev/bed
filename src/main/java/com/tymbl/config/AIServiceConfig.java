package com.tymbl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Configuration
public class AIServiceConfig {

    @Value("${ai.service.connection.timeout:30000}")
    private int connectionTimeout;

    @Value("${ai.service.read.timeout:60000}")
    private int readTimeout;

    @Value("${ai.service.request.timeout:90000}")
    private int requestTimeout;

    @Bean("aiServiceRestTemplate")
    public RestTemplate aiServiceRestTemplate() {
        log.info("Configuring AI Service RestTemplate with timeouts - Connection: {}ms, Read: {}ms, Request: {}ms", 
                connectionTimeout, readTimeout, requestTimeout);
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add request timeout interceptor
        restTemplate.getInterceptors().add((request, body, execution) -> {
            long startTime = System.currentTimeMillis();
            try {
                org.springframework.http.client.ClientHttpResponse response = execution.execute(request, body);
                long duration = System.currentTimeMillis() - startTime;
                log.debug("AI Service request completed in {}ms", duration);
                return response;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                if (duration >= requestTimeout) {
                    log.error("AI Service request timed out after {}ms", duration);
                    throw new RuntimeException("Request timeout after " + duration + "ms", e);
                }
                throw e;
            }
        });
        
        return restTemplate;
    }
} 