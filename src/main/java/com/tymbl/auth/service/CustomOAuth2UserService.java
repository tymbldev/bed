package com.tymbl.auth.service;

import com.tymbl.common.entity.Role;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String picture = (String) attributes.get("picture");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(email, firstName, lastName, picture));

        return oauth2User;
    }

    private User registerNewUser(String email, String firstName, String lastName, String picture) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setProfilePicture(picture);
        user.setEmailVerified(true);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    private String getEmailFromAttributes(Map<String, Object> attributes) {
        String email = null;
        if (attributes.containsKey("email")) {
            email = attributes.get("email").toString();
        } else if (attributes.containsKey("emailAddress")) {
            email = attributes.get("emailAddress").toString();
        }
        return email;
    }
} 