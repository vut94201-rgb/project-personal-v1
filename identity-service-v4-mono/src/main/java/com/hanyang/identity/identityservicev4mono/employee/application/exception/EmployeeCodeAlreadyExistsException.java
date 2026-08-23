package com.hanyang.identity.identityservicev4mono.employee.application.exception;

public class EmployeeCodeAlreadyExistsException
        extends RuntimeException {

    public EmployeeCodeAlreadyExistsException(String code) {
        super("Employee code already exists: " + code);
    }
}