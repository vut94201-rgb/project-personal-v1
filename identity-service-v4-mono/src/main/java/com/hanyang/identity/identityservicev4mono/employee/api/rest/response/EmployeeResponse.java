package com.hanyang.identity.identityservicev4mono.employee.api.rest.response;

import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String employeeCode,
        String fullName,
        String status
) {
}