package com.hanyang.identity.identityservicev4mono.employee.application.exception;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

public class EmployeeNotFoundException
        extends RuntimeException {

    public EmployeeNotFoundException(EmployeeId id) {
        super("Employee not found: " + id.value());
    }

    public EmployeeNotFoundException(String code) {
        super("Employee not found: " + code);
    }

}