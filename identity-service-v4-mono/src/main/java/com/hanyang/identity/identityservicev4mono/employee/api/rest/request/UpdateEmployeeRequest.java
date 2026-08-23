package com.hanyang.identity.identityservicev4mono.employee.api.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequest(

        @NotBlank
        @Size(max = 150)
        String fullName
) {
}