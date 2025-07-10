package com.tymbl.auth.filter;

import com.tymbl.auth.service.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Value("${server.servlet.context-path:/}")
    private String contextPath;
    
    // List of paths (without context path) that should bypass authentication
    private final List<String> publicPaths = Arrays.asList(
        "/api/v1/auth",
        "/api/v1/registration",
        "/api/v1/health",
        "/api/v1/jobsearch",
        "/api/v1/locations",
        "/api/v1/dropdowns",
        "/api/v1/crawler",
        "/api/admin/company-data",
        "/api/v1/skills",
        "/api/v1/companies"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        
        // Remove context path if present
        if (contextPath != null && !contextPath.equals("/") && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
        }
        
        // For public paths, check if the requested URI starts with any of them
        for (String publicPath : publicPaths) {
            if (requestURI.startsWith(publicPath)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\": 401, \"message\": \"Authorization header is missing or invalid\"}");
                return;
            }

            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication successful for user: {}", userEmail);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"status\": 401, \"message\": \"Invalid or expired token\"}");
                    return;
                }
            }
            
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error processing JWT authentication", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\": 401, \"message\": \"Authentication failed\"}");
        }
    }
} 