package com.hanyang.identity.identityservicev4mono.employee.application.exception;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;

public class EmployeeNationalIdentityNotFoundException extends RuntimeException {

    public EmployeeNationalIdentityNotFoundException(
            EmployeeId employeeId,
            String countryCode,
            NationalIdentityType identityType
    ) {
        super("Employee national identity not found for employee: "
                + employeeId.value() + " (" + countryCode + "/" + identityType + ")");
    }
}