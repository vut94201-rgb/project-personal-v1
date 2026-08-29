package com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateServicePrincipalRequest(
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*")
        String code,

        @NotBlank
        @Size(max = 150)
        String displayName,

        @NotBlank
        @Size(max = 500)
        String purpose,

        @Size(max = 1000)
        String description,

        @NotNull
        UUID primaryOwnerEmployeeId
) {
}