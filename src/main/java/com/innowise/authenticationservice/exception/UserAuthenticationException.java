package com.innowise.authenticationservice.exception;

public class UserAuthenticationException extends RuntimeException {

  public UserAuthenticationException() {
  }

  public UserAuthenticationException(String message) {
    super(message);
  }

  public UserAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserAuthenticationException(Throwable cause) {
    super(cause);
  }
}