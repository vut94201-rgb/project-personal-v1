package com.hanyang.identity.identityservicev4mono.employee.application.command;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

import java.time.LocalDate;

public record UpdateEmployeeProfileCommand(
        EmployeeId employeeId,
        String email,
        String phoneNumber,
        String address,
        LocalDate hireDate
) {
}