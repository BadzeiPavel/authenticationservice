package com.innowise.authenticationservice.exception;

public class DownstreamServiceException extends RuntimeException {

  public DownstreamServiceException() {
  }

  public DownstreamServiceException(String message) {
    super(message);
  }

  public DownstreamServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public DownstreamServiceException(Throwable cause) {
    super(cause);
  }
}