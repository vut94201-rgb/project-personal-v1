package com.hanyang.identity.identityservicev4mono.employee.domain;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmployeeProfileTest {

    @Test
    void createNormalizesOptionalTextAndEmail() {
        EmployeeId employeeId = EmployeeId.newId();

        EmployeeProfile profile = EmployeeProfile.create(
                employeeId,
                "  USER@HANYANG.COM  ",
                "  0901234567  ",
                "  Bac Ninh  ",
                LocalDate.of(2026, 8, 25)
        );

        assertEquals(employeeId, profile.getEmployeeId());
        assertEquals("user@hanyang.com", profile.getEmail());
        assertEquals("0901234567", profile.getPhoneNumber());
        assertEquals("Bac Ninh", profile.getAddress());
        assertEquals(LocalDate.of(2026, 8, 25), profile.getHireDate());
    }

    @Test
    void blankOptionalValuesBecomeNull() {
        EmployeeProfile profile = EmployeeProfile.create(
                EmployeeId.newId(),
                " ",
                "",
                "   ",
                null
        );

        assertNull(profile.getEmail());
        assertNull(profile.getPhoneNumber());
        assertNull(profile.getAddress());
        assertNull(profile.getHireDate());
    }
}