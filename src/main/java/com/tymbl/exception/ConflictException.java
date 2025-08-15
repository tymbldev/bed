package com.tymbl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

  private Long jobId;

  public ConflictException(String message) {
    super(message);
  }

  public ConflictException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConflictException(String message, Long jobId) {
    super(message);
    this.jobId = jobId;
  }

  public Long getJobId() {
    return jobId;
  }
} 