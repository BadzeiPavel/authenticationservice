package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.model.dto.request.LoginRequest;
import com.innowise.authenticationservice.model.dto.request.RegisterRequest;
import com.innowise.authenticationservice.model.dto.response.TokenResponse;
import com.innowise.authenticationservice.model.dto.response.TokenValidationResponse;

public interface AuthService {

  void register(RegisterRequest request);

  TokenResponse login(LoginRequest request);

  TokenValidationResponse validate(String token);

  TokenResponse refresh(String refreshToken);
}