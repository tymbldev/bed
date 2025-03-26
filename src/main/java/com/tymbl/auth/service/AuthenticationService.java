package com.tymbl.auth.service;

import com.tymbl.auth.dto.AuthResponse;
import com.tymbl.auth.dto.LoginRequest;

public interface AuthenticationService {
    AuthResponse login(LoginRequest request);
    
    void verifyEmailToken(String token);
    
    void initiatePasswordReset(String email) throws Exception;
    
    void resetPassword(String token, String newPassword);
    
    void resendVerificationEmail(String email) throws Exception;
} 