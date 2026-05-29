package com.innowise.authenticationservice.client;

import com.innowise.commonstarter.model.dto.UserDto;
import com.innowise.commonstarter.model.dto.request.UserCreationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "userservice", url = "${service.user-service.url}")
public interface UserServiceClient {

  @PostMapping("/api/v1/users")
  ResponseEntity<UserDto> createUser(@RequestBody UserCreationDto request);
}