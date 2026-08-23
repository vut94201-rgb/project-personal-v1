package com.hanyang.identity.identityservicev4mono.access.api.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRoleRequest(

        @NotNull
        UUID applicationId,

        @NotBlank
        @Size(max = 50)
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z0-9_]*$",
                message = "must start with a letter and contain only letters, digits or underscore"
        )
        String code,

        @NotBlank
        @Size(max = 150)
        String name
) {
}