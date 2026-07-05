package com.personal.identity.account.application.dto.request;

import com.personal.identity.account.domain.enums.Gender;
import com.personal.identity.account.domain.enums.UserStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdateIdentityUserRequest(
    @Email  @Size(max = 255) String email,
    @Size(max = 20) String phoneNumber,
    Gender gender,
    @Past
    LocalDate dateOfBirth,
    UserStatus status) {}
