package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;

public class DepartmentNotFoundException extends RuntimeException {
    public DepartmentNotFoundException(DepartmentId id) {
        super("Department not found: " + id.value());
    }

    public DepartmentNotFoundException(String code) {
        super("Department not found: " + code);
    }
}
