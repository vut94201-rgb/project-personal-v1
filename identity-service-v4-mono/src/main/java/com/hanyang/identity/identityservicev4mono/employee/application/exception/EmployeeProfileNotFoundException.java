package com.hanyang.identity.identityservicev4mono.employee.application.exception;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

public class EmployeeProfileNotFoundException extends RuntimeException {
    public EmployeeProfileNotFoundException(EmployeeId employeeId) {
        super("Employee profile not found: " + employeeId.value());
    }
}