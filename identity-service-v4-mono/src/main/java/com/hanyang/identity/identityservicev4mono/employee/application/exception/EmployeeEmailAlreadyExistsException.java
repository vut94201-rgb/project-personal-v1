package com.hanyang.identity.identityservicev4mono.employee.application.exception;

public class EmployeeEmailAlreadyExistsException extends RuntimeException {
    public EmployeeEmailAlreadyExistsException(String email) {
        super("Employee email already exists: " + email);
    }
}