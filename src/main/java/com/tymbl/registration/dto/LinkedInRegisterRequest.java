package com.tymbl.registration.dto;

import lombok.Data;

@Data
public class LinkedInRegisterRequest {
    private String accessToken;
    private String linkedInProfileUrl;
} 