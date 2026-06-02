package com.innowise.authenticationservice.exception;

public class DownstreamValidationException extends RuntimeException {

  public DownstreamValidationException() {
  }

  public DownstreamValidationException(String message) {
    super(message);
  }

  public DownstreamValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public DownstreamValidationException(Throwable cause) {
    super(cause);
  }
}