package com.hanyang.identity.identityservicev4mono.organization.api.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePositionRequest(
        @NotBlank
        @Size(max = 150)
        String name
) {
}
