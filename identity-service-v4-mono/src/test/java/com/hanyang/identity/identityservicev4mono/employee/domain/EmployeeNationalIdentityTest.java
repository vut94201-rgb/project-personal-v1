package com.hanyang.identity.identityservicev4mono.employee.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeNationalIdentityTest {

    @Test
    void createsMaskedRepresentationWithoutExposingFullNumber() {
        EmployeeNationalIdentity identity = EmployeeNationalIdentity.create(
                EmployeeNationalIdentityId.newId(),
                EmployeeId.newId(),
                "vn",
                NationalIdentityType.NATIONAL_ID_CARD,
                "v1:iv:ciphertext",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "2345"
        );

        assertEquals("VN", identity.getCountryCode());
        assertEquals("********2345", identity.getMaskedNumber());
    }
}