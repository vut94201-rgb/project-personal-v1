package com.hanyang.identity.identityservicev4mono.account.api.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAccountRequest(

        @NotNull
        UUID employeeId,

        @NotBlank
        @Size(max = 100)
        String username
) {
}