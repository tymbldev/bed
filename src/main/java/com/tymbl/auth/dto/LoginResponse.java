package com.tymbl.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private String token;
  private String email;
  private String firstName;
  private String lastName;
  private String role;
  private boolean emailVerified;
} 