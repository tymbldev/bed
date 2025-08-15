package com.tymbl.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.registration.dto.LinkedInProfile;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class LinkedInService {

  private final UserRepository userRepository;
  private final RestTemplate restTemplate;

  @Value("${linkedin.client-id}")
  private String clientId;

  @Value("${linkedin.client-secret}")
  private String clientSecret;

  @Value("${linkedin.redirect-uri}")
  private String redirectUri;

  /**
   * Validates a LinkedIn access token and retrieves the user. Throws an exception if user is not
   * found.
   */
  public User validateAndLogin(String accessToken) {
    // Validate the access token with LinkedIn
    // First, get basic profile information
    String userInfoUrl = "https://api.linkedin.com/v2/me";
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(
        userInfoUrl,
        HttpMethod.GET,
        entity,
        Map.class
    );

    if (response.getBody() == null) {
      throw new RuntimeException("Failed to get user info from LinkedIn");
    }

    // Get email address from a separate endpoint
    String emailUrl = "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))";
    ResponseEntity<Map> emailResponse = restTemplate.exchange(
        emailUrl,
        HttpMethod.GET,
        entity,
        Map.class
    );

    if (emailResponse.getBody() == null) {
      throw new RuntimeException("Failed to get email from LinkedIn");
    }

    // Extract user information
    Map<String, Object> userInfo = response.getBody();
    Map<String, Object> emailData = emailResponse.getBody();

    // Extract appropriate fields based on LinkedIn API response structure
    Map<String, Object> localizedFirstName = (Map<String, Object>) userInfo.get(
        "localizedFirstName");
    Map<String, Object> localizedLastName = (Map<String, Object>) userInfo.get("localizedLastName");

    String firstName = userInfo.containsKey("localizedFirstName") ?
        (String) userInfo.get("localizedFirstName") :
        "LinkedIn User";

    String lastName = userInfo.containsKey("localizedLastName") ?
        (String) userInfo.get("localizedLastName") :
        "";

    // Extract email from response
    String email = extractEmailFromLinkedInResponse(emailData);

    if (email == null || email.isEmpty()) {
      throw new RuntimeException("Could not retrieve email from LinkedIn");
    }

    // Find user and throw error if not found
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found. Please register first."));
  }

  /**
   * Retrieves LinkedIn profile data for registration purposes. This is used when creating a new
   * user via LinkedIn.
   */
  public LinkedInProfile getProfileData(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(
        "https://api.linkedin.com/v2/me?projection=(id,localizedFirstName,localizedLastName,profilePicture(displayImage~:playableStreams),email-address)",
        HttpMethod.GET,
        entity,
        String.class
    );

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(response.getBody(), LinkedInProfile.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse LinkedIn profile data", e);
    }
  }

  private String extractEmailFromLinkedInResponse(Map<String, Object> emailData) {
    try {
      if (emailData.containsKey("elements") && emailData.get("elements") instanceof Iterable) {
        Iterable<?> elements = (Iterable<?>) emailData.get("elements");
        for (Object element : elements) {
          if (element instanceof Map) {
            Map<?, ?> elementMap = (Map<?, ?>) element;
            if (elementMap.containsKey("handle~")) {
              Map<?, ?> handleMap = (Map<?, ?>) elementMap.get("handle~");
              if (handleMap.containsKey("emailAddress")) {
                return (String) handleMap.get("emailAddress");
              }
            }
          }
        }
      }
    } catch (Exception e) {
      // Log error and continue to throw the general exception
    }
    return null;
  }
} 