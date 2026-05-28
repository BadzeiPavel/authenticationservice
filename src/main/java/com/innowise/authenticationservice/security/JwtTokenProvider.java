package com.innowise.authenticationservice.security;

import com.innowise.authenticationservice.config.jwt.JwtSecrets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final JwtSecrets jwtSecrets;
  private final SecretKey key;

  public JwtTokenProvider(JwtSecrets jwtSecrets) {
    this.jwtSecrets = jwtSecrets;
    this.key = Keys.hmacShaKeyFor(jwtSecrets.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(UUID userId, String role) {
    return Jwts.builder()
        .subject(userId.toString())
        .claim("role", role)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtSecrets.accessTokenExpiration()))
        .signWith(key)
        .compact();
  }

  public String generateRefreshToken(UUID userId) {
    return Jwts.builder()
        .subject(userId.toString())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtSecrets.refreshTokenExpiration()))
        .signWith(key)
        .compact();
  }

  public Claims validateToken(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}