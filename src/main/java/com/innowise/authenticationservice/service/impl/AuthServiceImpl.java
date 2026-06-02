package com.innowise.authenticationservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.authenticationservice.client.UserServiceClient;
import com.innowise.authenticationservice.exception.DownstreamServiceException;
import com.innowise.authenticationservice.exception.DownstreamValidationException;
import com.innowise.authenticationservice.exception.UserAlreadyExistsException;
import com.innowise.authenticationservice.exception.UserAuthenticationException;
import com.innowise.authenticationservice.mapper.UserCredentialMapper;
import com.innowise.authenticationservice.model.dto.request.LoginRequest;
import com.innowise.authenticationservice.model.dto.request.RegisterRequest;
import com.innowise.authenticationservice.model.dto.response.TokenResponse;
import com.innowise.authenticationservice.model.dto.response.TokenValidationResponse;
import com.innowise.authenticationservice.model.entity.UserCredential;
import com.innowise.authenticationservice.repository.UserCredentialRepository;
import com.innowise.authenticationservice.security.JwtTokenGenerationProvider;
import com.innowise.authenticationservice.service.AuthService;
import com.innowise.commonstarter.model.dto.UserDto;
import com.innowise.commonstarter.security.JwtTokenProvider;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserCredentialRepository credentialRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtTokenGenerationProvider jwtTokenGenerationProvider;
  private final UserCredentialMapper userCredentialMapper;
  private final UserServiceClient userServiceClient;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void register(RegisterRequest request) {
    if (credentialRepository.existsByUsername(request.username())) {
      throw new UserAlreadyExistsException("Username already exists");
    }

    UUID userId = null;
    try {
      UserDto createdUser = createUser(request);
      userId = createdUser.id();

      persistUserCredentials(request, userId);
    } catch (FeignException e) {
      deleteUser(userId);
      throw mapFeignException(e);
    }
  }

  private UserDto createUser(RegisterRequest request) {
    UserDto createdUser = userServiceClient.createUser(request.userDetails()).getBody();
    if (createdUser == null) {
      throw new UserAuthenticationException("Failed to create user");
    }
    return createdUser;
  }

  private void persistUserCredentials(RegisterRequest request, UUID userId) {
    UserCredential cred = userCredentialMapper.toEntity(request);
    cred.setUserId(userId);
    cred.setPassword(passwordEncoder.encode(cred.getPassword()));
    credentialRepository.save(cred);
  }

  private void deleteUser(UUID userId) {
    if (userId != null) {
      try {
        userServiceClient.deleteUser(userId, true);
      } catch (Exception ex) {
        log.warn("Failed to delete user {} during compensation", userId, ex);
      }
    }
  }

  @Override
  public TokenResponse login(LoginRequest request) {
    UserCredential cred = credentialRepository.findByUsername(request.username())
        .orElseThrow(() -> new UserAuthenticationException("Invalid credentials"));
    if (!passwordEncoder.matches(request.password(), cred.getPassword())) {
      throw new UserAuthenticationException("Invalid credentials");
    }

    String access = jwtTokenGenerationProvider.generateAccessToken(cred.getUserId(),
        cred.getRole().name());
    String refresh = jwtTokenGenerationProvider.generateRefreshToken(cred.getUserId());

    return new TokenResponse(access, refresh);
  }

  @Override
  public TokenValidationResponse validate(String token) {
    Claims claims = jwtTokenProvider.validateToken(token);
    return new TokenValidationResponse(
        UUID.fromString(claims.getSubject()),
        claims.get("role", String.class),
        true
    );
  }

  @Override
  public TokenResponse refresh(String refreshToken) {
    Claims claims = jwtTokenProvider.validateToken(refreshToken);
    if (!"refresh".equals(claims.get("type"))) {
      throw new UserAuthenticationException("Token is not a refresh token");
    }

    UUID userId = UUID.fromString(claims.getSubject());
    UserCredential cred = credentialRepository.findByUserId(userId)
        .orElseThrow(() -> new UserAuthenticationException("User not found"));

    String newAccess = jwtTokenGenerationProvider.generateAccessToken(userId,
        cred.getRole().name());
    String newRefresh = jwtTokenGenerationProvider.generateRefreshToken(userId);

    return new TokenResponse(newAccess, newRefresh);
  }

  private RuntimeException mapFeignException(FeignException e) {
    String message = extractExceptionMessage(e);
    return switch (e.status()) {
      case 400 -> new DownstreamValidationException(message);
      case 409 -> new UserAlreadyExistsException(message);
      default -> new DownstreamServiceException(
          "User service error: " + e.status() + " – " + message);
    };
  }

  private String extractExceptionMessage(FeignException e) {
    try {
      String json = e.contentUTF8();
      JsonNode root = objectMapper.readTree(json);
      if (root.has("message")) {
        return root.get("message").asText();
      }
    } catch (JsonProcessingException ex) {
      throw new UserAuthenticationException("Failed to parse json response");
    }
    return e.getMessage();
  }
}