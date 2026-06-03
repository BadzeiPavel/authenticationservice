package com.innowise.authenticationservice.client;

import com.innowise.commonstarter.model.dto.UserDto;
import com.innowise.commonstarter.model.dto.request.UserCreationDto;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "userservice", url = "${service.user-service.url}")
public interface UserServiceClient {

  @PostMapping("/api/v1/users")
  ResponseEntity<UserDto> createUser(@RequestBody UserCreationDto request);

  @DeleteMapping("/api/v1/users/{id}")
  ResponseEntity<Void> deleteUser(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "false") boolean hardDeletion);
}