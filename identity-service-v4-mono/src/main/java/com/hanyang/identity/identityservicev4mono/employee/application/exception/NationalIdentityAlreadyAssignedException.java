package com.hanyang.identity.identityservicev4mono.employee.application.exception;

import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;

public class NationalIdentityAlreadyAssignedException extends RuntimeException {

    public NationalIdentityAlreadyAssignedException(
            String countryCode,
            NationalIdentityType identityType
    ) {
        super("National identity is already assigned to another employee: "
                + countryCode + "/" + identityType);
    }
}