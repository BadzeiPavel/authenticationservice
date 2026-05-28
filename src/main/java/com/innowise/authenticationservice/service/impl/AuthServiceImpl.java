package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.client.UserServiceClient;
import com.innowise.authenticationservice.exception.AuthenticationException;
import com.innowise.authenticationservice.mapper.UserCredentialMapper;
import com.innowise.authenticationservice.model.dto.request.LoginRequest;
import com.innowise.authenticationservice.model.dto.request.RegisterRequest;
import com.innowise.authenticationservice.model.dto.response.TokenResponse;
import com.innowise.authenticationservice.model.dto.response.TokenValidationResponse;
import com.innowise.authenticationservice.model.entity.UserCredential;
import com.innowise.authenticationservice.repository.UserCredentialRepository;
import com.innowise.authenticationservice.security.JwtTokenProvider;
import com.innowise.authenticationservice.service.AuthService;
import com.innowise.common.model.dto.UserDto;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserCredentialRepository credentialRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtProvider;
  private final UserCredentialMapper userCredentialMapper;
  private final UserServiceClient userServiceClient;

  @Override
  @Transactional
  public void register(RegisterRequest request) {
    if (credentialRepository.findByUsername(request.username()).isPresent()) {
      throw new AuthenticationException("Username already exists");
    }

    UserDto user = userServiceClient.createUser(request.userDetails()).getBody();
    if (user == null) {
      throw new AuthenticationException("Failed to create user");
    }

    UserCredential cred = userCredentialMapper.toEntity(request);
    cred.setUserId(user.id());
    cred.setPassword(passwordEncoder.encode(cred.getPassword()));
    credentialRepository.save(cred);
  }

  @Override
  public TokenResponse login(LoginRequest request) {
    UserCredential cred = credentialRepository.findByUsername(request.username())
        .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
    if (!passwordEncoder.matches(request.password(), cred.getPassword())) {
      throw new AuthenticationException("Invalid credentials");
    }

    String access = jwtProvider.generateAccessToken(cred.getUserId(), cred.getRole().name());
    String refresh = jwtProvider.generateRefreshToken(cred.getUserId());

    return new TokenResponse(access, refresh);
  }

  @Override
  public TokenValidationResponse validate(String token) {
    try {
      Claims claims = jwtProvider.validateToken(token);
      return new TokenValidationResponse(
          UUID.fromString(claims.getSubject()),
          claims.get("role", String.class),
          true
      );
    } catch (Exception e) {
      return new TokenValidationResponse(null, null, false);
    }
  }

  @Override
  public TokenResponse refresh(String refreshToken) {
    Claims claims = jwtProvider.validateToken(refreshToken);
    UUID userId = UUID.fromString(claims.getSubject());

    UserCredential cred = credentialRepository.findByUserId(userId)
        .orElseThrow(() -> new AuthenticationException("User not found"));

    String newAccess = jwtProvider.generateAccessToken(userId, cred.getRole().name());
    String newRefresh = jwtProvider.generateRefreshToken(userId);

    return new TokenResponse(newAccess, newRefresh);
  }
}