package com.hanyang.identity.identityservicev4mono.employee.api.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateEmployeeProfileRequest(
        @Email
        @Size(max = 254)
        String email,

        @Size(max = 50)
        String phoneNumber,

        @Size(max = 500)
        String address,

        LocalDate hireDate
) {
}