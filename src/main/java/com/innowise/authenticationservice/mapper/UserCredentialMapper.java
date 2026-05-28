package com.innowise.authenticationservice.mapper;

import com.innowise.authenticationservice.model.dto.request.RegisterRequest;
import com.innowise.authenticationservice.model.entity.UserCredential;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserCredentialMapper {

  @Mapping(target = "id", ignore = true)
  UserCredential toEntity(RegisterRequest request);
}