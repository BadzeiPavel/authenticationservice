package com.innowise.authenticationservice.controller;

import com.innowise.authenticationservice.exception.DownstreamServiceException;
import com.innowise.authenticationservice.exception.DownstreamValidationException;
import com.innowise.authenticationservice.exception.UserAlreadyExistsException;
import com.innowise.authenticationservice.exception.UserAuthenticationException;
import com.innowise.commonstarter.model.dto.response.ErrorResponse;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(UserAuthenticationException ex,
      HttpServletRequest request) {
    return buildResponse("User authentication failed", ex.getMessage(),
        ex.getClass().getSimpleName(), HttpStatus.UNAUTHORIZED, request);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleSpringAuth(AuthenticationException ex,
      HttpServletRequest request) {
    return buildResponse("Authentication failed", ex.getMessage(),
        ex.getClass().getSimpleName(), HttpStatus.UNAUTHORIZED, request);
  }

  @ExceptionHandler({JwtException.class, SignatureException.class})
  public ResponseEntity<ErrorResponse> handleJwt(JwtException ex,
      HttpServletRequest request) {
    return buildResponse("Invalid token", ex.getMessage(),
        ex.getClass().getSimpleName(), HttpStatus.UNAUTHORIZED, request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
      HttpServletRequest request) {
    return buildResponse("Access Denied", ex.getMessage(),
        ex.getClass().getSimpleName(), HttpStatus.FORBIDDEN, request);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleConflict(UserAlreadyExistsException ex,
      HttpServletRequest request) {
    return buildResponse("Conflict", ex.getMessage(),
        ex.getClass().getSimpleName(), HttpStatus.CONFLICT, request);
  }

  @ExceptionHandler(DownstreamValidationException.class)
  public ResponseEntity<ErrorResponse> handleDownstreamBadRequest(DownstreamValidationException ex,
      HttpServletRequest request) {
    return buildResponse("Bad Request", ex.getMessage(),
        ex.getClass().getSimpleName(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(DownstreamServiceException.class)
  public ResponseEntity<ErrorResponse> handleDownstreamError(DownstreamServiceException ex,
      HttpServletRequest request) {
    return buildResponse("Service Unavailable", ex.getMessage(),
        ex.getClass().getSimpleName(), HttpStatus.BAD_GATEWAY, request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
      HttpServletRequest request) {
    return buildResponse("Bad Request", ex.getMessage(),
        ex.getClass().getSimpleName(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    String errors = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .collect(Collectors.joining("; "));
    return buildResponse("Validation Failed", errors,
        ex.getClass().getSimpleName(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    return buildResponse("Internal Server Error",
        "An unexpected error occurred",
        ex.getClass().getSimpleName(),
        HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  private ResponseEntity<ErrorResponse> buildResponse(String title, String message,
      String exceptionName,
      HttpStatus status,
      HttpServletRequest request) {
    ErrorResponse response = ErrorResponse.builder()
        .title(title)
        .name(exceptionName)
        .status(status.value())
        .message(message)
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();
    return new ResponseEntity<>(response, status);
  }
}