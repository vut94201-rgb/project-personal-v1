package com.hanyang.identity.identityservicev4mono.employee.application.command;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;

public record UpsertEmployeeNationalIdentityCommand(
        EmployeeId employeeId,
        String countryCode,
        NationalIdentityType identityType,
        String number
) {
}