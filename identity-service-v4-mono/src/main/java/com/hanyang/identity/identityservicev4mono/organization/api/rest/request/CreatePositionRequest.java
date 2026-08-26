package com.hanyang.identity.identityservicev4mono.organization.api.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePositionRequest(
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
