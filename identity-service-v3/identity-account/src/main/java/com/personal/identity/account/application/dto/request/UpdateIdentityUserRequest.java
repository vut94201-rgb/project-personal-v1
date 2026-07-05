package com.personal.identity.account.application.dto.request;

import com.personal.identity.account.domain.enums.Gender;
import com.personal.identity.account.domain.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateIdentityUserRequest(
    @Email @NotBlank @Size(max = 255) String email,
    @Size(max = 20)  String phoneNumber,
    Gender gender,
    @Past
    LocalDate dateOfBirth,
    UserStatus status) {}
