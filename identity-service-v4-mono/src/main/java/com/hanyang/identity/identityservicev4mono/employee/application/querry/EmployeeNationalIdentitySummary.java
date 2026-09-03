package com.hanyang.identity.identityservicev4mono.employee.application.querry;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentityId;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;

public record EmployeeNationalIdentitySummary(
        EmployeeNationalIdentityId id,
        EmployeeId employeeId,
        String countryCode,
        NationalIdentityType identityType,
        String maskedNumber
) {
}