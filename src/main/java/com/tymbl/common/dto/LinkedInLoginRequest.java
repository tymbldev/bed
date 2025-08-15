package com.tymbl.common.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkedInLoginRequest {

  @NotBlank(message = "LinkedIn access token is required")
  private String accessToken;
} 