package com.hanyang.identity.identityservicev4mono.access.api.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateApplicationRequest(

        @NotBlank
        @Size(max = 150)
        String name
) {
}