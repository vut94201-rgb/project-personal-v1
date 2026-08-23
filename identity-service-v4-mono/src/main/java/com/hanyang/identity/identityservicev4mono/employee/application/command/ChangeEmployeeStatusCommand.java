package com.hanyang.identity.identityservicev4mono.employee.application.command;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;

public record ChangeEmployeeStatusCommand(
        EmployeeId employeeId,
        EmployeeStatus status
) {
}