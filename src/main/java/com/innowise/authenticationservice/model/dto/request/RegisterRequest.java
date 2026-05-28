package com.innowise.authenticationservice.model.dto.request;

import com.innowise.authenticationservice.model.entity.UserCredential;
import com.innowise.common.model.dto.request.UserCreationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank String password,
    @NotNull UserCredential.Role role,
    @Valid @NotNull UserCreationDto userDetails
) {

}