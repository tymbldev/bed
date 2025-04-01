package com.tymbl.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LinkedInLoginRequest {
    @NotBlank(message = "LinkedIn access token is required")
    private String accessToken;
} 