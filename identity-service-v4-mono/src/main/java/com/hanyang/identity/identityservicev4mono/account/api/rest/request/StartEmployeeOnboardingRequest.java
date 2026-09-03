package com.hanyang.identity.identityservicev4mono.account.api.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


public record StartEmployeeOnboardingRequest(

        @NotBlank
        @Size(max = 150)
        String fullName,

        @NotBlank
        @Size(max = 100)
        String username,

        @NotBlank
        @Pattern(
                regexp = "\\d{12}",
                message = "nationalIdentityNumber must contain exactly 12 digits"
        )
        String nationalIdentityNumber,

        @Email
        @Size(max = 254)
        String email,

        @Size(max = 50)
        String phone,

        @Size(max = 500)
        String address,

        LocalDate joinDate
) {
}