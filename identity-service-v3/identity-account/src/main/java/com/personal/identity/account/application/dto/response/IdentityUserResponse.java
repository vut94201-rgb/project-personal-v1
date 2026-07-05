package com.personal.identity.account.application.dto.response;

import com.personal.identity.account.domain.enums.Gender;
import com.personal.identity.account.domain.enums.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record IdentityUserResponse(
    Long id,
    UUID keycloakUserId,
    String username,
    String email,
    String phoneNumber,
    Gender gender,
    UserStatus status,
    LocalDate dateOfBirth,
    Instant createdAt,
    Instant updatedAt) {}
