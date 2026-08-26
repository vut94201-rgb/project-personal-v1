package com.hanyang.identity.identityservicev4mono.employee.api.rest.response;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeProfileResponse(
        UUID employeeId,
        String email,
        String phoneNumber,
        String address,
        LocalDate hireDate
) {
}