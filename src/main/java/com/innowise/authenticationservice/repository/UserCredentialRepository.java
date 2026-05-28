package com.innowise.authenticationservice.repository;

import com.innowise.authenticationservice.model.entity.UserCredential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {

  Optional<UserCredential> findByUsername(String username);

  Optional<UserCredential> findByUserId(UUID userId);
}