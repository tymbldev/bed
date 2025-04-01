package com.tymbl.common.service;

import com.tymbl.common.entity.User;
import com.tymbl.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LinkedInService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${linkedin.client-id}")
    private String clientId;

    @Value("${linkedin.client-secret}")
    private String clientSecret;

    @Value("${linkedin.redirect-uri}")
    private String redirectUri;

    public String validateAndLogin(String accessToken) {
        // Validate the access token with LinkedIn
        Map<String, Object> userInfo = validateAccessToken(accessToken);
        
        // Extract LinkedIn user information
        String email = (String) userInfo.get("email");
        String firstName = (String) userInfo.get("given_name");
        String lastName = (String) userInfo.get("family_name");
        String linkedInProfile = (String) userInfo.get("picture");

        // Find or create user
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createNewUser(email, firstName, lastName, linkedInProfile));

        // Generate JWT token
        return jwtService.generateToken(user);
    }

    private Map<String, Object> validateAccessToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            "https://api.linkedin.com/v2/me?projection=(id,localizedFirstName,localizedLastName,profilePicture(displayImage~:playableStreams))",
            HttpMethod.GET,
            entity,
            Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Invalid LinkedIn access token");
        }

        return response.getBody();
    }

    private User createNewUser(String email, String firstName, String lastName, String linkedInProfile) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setLinkedInProfile(linkedInProfile);
        user.setEnabled(true);
        user.setRole(User.Role.USER);
        
        return userRepository.save(user);
    }
} 