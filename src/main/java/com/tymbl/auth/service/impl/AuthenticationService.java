package com.tymbl.auth.service.impl;

import com.tymbl.auth.dto.AuthResponse;
import com.tymbl.auth.dto.LoginRequest;
import com.tymbl.auth.service.JwtService;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.common.service.EmailService;
import com.tymbl.common.service.LinkedInService;
import java.util.UUID;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final LinkedInService linkedInService;

    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    
    @Transactional
    public void verifyEmailToken(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
    }

    
    @Transactional
    public void initiatePasswordReset(String email) throws javax.mail.MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(System.currentTimeMillis() + 3600000); // 1 hour
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getPasswordResetTokenExpiry() < System.currentTimeMillis()) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPassword(newPassword);
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    
    @Transactional
    public void resendVerificationEmail(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }

    public AuthResponse loginWithLinkedIn(String accessToken) {
        User user = linkedInService.validateAndLogin(accessToken);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getEmail(), null, user.getAuthorities());
        
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
            .token(token)
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole().name())
            .emailVerified(user.isEmailVerified())
            .build();
    }

} 