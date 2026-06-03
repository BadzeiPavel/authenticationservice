package com.innowise.authenticationservice.security;

import com.innowise.commonstarter.config.jwt.JwtSecrets;
import com.innowise.commonstarter.security.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenGenerationProvider {

  private final JwtSecrets jwtSecrets;
  private final JwtTokenProvider jwtTokenProvider;

  public String generateAccessToken(UUID userId, String role) {
    return Jwts.builder()
        .subject(userId.toString())
        .claim("role", role)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtSecrets.accessTokenExpiration()))
        .signWith(jwtTokenProvider.getKey())
        .compact();
  }

  public String generateRefreshToken(UUID userId) {
    return Jwts.builder()
        .subject(userId.toString())
        .claim("type", "refresh")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtSecrets.refreshTokenExpiration()))
        .signWith(jwtTokenProvider.getKey())
        .compact();
  }

}
