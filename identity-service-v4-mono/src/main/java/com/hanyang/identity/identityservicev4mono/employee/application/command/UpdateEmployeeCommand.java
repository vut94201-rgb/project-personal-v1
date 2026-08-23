package com.hanyang.identity.identityservicev4mono.employee.application.command;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

public record UpdateEmployeeCommand(
        EmployeeId employeeId,
        String fullName
) {
}