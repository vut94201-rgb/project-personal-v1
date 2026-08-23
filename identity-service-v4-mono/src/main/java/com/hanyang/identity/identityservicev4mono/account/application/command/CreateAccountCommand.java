package com.hanyang.identity.identityservicev4mono.account.application.command;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

public record CreateAccountCommand(
        EmployeeId employeeId,
        String username
) {
}