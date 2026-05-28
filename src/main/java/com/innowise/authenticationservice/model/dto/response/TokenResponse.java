package com.innowise.authenticationservice.model.dto.response;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {

}