package com.tymbl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmailAlreadyExistsException extends RuntimeException {

  public EmailAlreadyExistsException(String email) {
    super("User with email '" + email + "' already exists");
  }

  public EmailAlreadyExistsException(String email, Throwable cause) {
    super("User with email '" + email + "' already exists", cause);
  }
} 