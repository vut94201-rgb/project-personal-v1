package com.hanyang.identity.identityservicev4mono.account.application;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentityId;

public record EmployeeOnboardingResult(
        EmployeeId employeeId,
        String employeeCode,
        EmployeeNationalIdentityId nationalIdentityId,
        String maskedNationalIdentity
) {
}