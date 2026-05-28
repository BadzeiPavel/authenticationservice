package com.innowise.authenticationservice.model.dto.response;

import java.util.UUID;

public record TokenValidationResponse(
    UUID userId,
    String role,
    boolean valid
) {

}