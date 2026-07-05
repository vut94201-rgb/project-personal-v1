package com.personal.identity.account.application.dto.request;

import com.personal.identity.account.domain.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateIdentityUserRequest(
    @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters")
        String username,
    @NotBlank @Email @Size(max = 255) String email,
    @Size(max = 20) String phoneNumber,
    @NotNull LocalDate dateOfBirth,
    @NotNull Gender gender) {}
