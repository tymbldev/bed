package com.tymbl.common.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@Order(1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Wrap the request and response to allow reading the body multiple times
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Pass wrapped request and response to the next filter
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log request
            logRequest(requestWrapper);
            
            // Log response
            logResponse(responseWrapper, duration);
            
            // Important: copy content back to the response
            responseWrapper.copyBodyToResponse();
        }
    }
    
    private void logRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        
        // Log request details
        logger.info("REQUEST: {} {}{}", method, uri, 
                (queryString != null ? "?" + queryString : ""));
        
        // Log headers
        Collections.list(request.getHeaderNames())
                .forEach(headerName -> {
                    String headerValue = request.getHeader(headerName);
                    logger.debug("Request Header: {} = {}", headerName, headerValue);
                });
        
        // Log request body for POST, PUT, or PATCH methods
        if (isLoggableRequestMethod(method) && request.getContentLength() > 0) {
            String requestBody = getRequestBody(request);
            logger.info("Request Body: {}", requestBody);
        }
    }
    
    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        int status = response.getStatus();
        logger.info("RESPONSE: {} ({}ms)", status, duration);
        
        // Log headers
        response.getHeaderNames()
                .forEach(headerName -> {
                    String headerValue = response.getHeader(headerName);
                    logger.debug("Response Header: {} = {}", headerName, headerValue);
                });
        
        // Log response body if there's an error (4xx or 5xx)
        if (status >= 400 && response.getContentSize() > 0) {
            String responseBody = getResponseBody(response);
            logger.info("Response Body: {}", responseBody);
        }
    }
    
    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            try {
                return new String(content, request.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                logger.error("Failed to parse request body", e);
                return "[Body parsing failed]";
            }
        }
        return "[Empty body]";
    }
    
    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            try {
                return new String(content, response.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                logger.error("Failed to parse response body", e);
                return "[Body parsing failed]";
            }
        }
        return "[Empty body]";
    }
    
    private boolean isLoggableRequestMethod(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }
} 