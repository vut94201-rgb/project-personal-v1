package com.hanyang.identity.identityservicev4mono.organization.application.exception;

public class DepartmentCodeAlreadyExistsException extends RuntimeException {
    public DepartmentCodeAlreadyExistsException(String code) {
        super("Department code already exists: " + code);
    }
}
