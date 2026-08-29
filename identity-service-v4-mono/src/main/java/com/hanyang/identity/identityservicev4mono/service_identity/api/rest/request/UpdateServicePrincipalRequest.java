package com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateServicePrincipalRequest(
        @NotBlank
        @Size(max = 150)
        String displayName,

        @NotBlank
        @Size(max = 500)
        String purpose,

        @Size(max = 1000)
        String description
) {
}