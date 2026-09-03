package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentityId;

public record EmployeeOnboardingResult(
        EmployeeId employeeId,
        String employeeCode,
        EmployeeNationalIdentityId nationalIdentityId,
        String maskedNationalIdentity,
        AccountId accountId,
        String username,
        AccountStatus accountStatus
) {
}