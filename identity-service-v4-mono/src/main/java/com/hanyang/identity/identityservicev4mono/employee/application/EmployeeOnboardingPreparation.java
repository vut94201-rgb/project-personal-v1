package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;

public record EmployeeOnboardingPreparation(
        EmployeeId employeeId,
        String employeeCode,
        String countryCode,
        NationalIdentityType identityType,
        String encryptedNumber,
        String numberFingerprint,
        String lastFour
) {
}