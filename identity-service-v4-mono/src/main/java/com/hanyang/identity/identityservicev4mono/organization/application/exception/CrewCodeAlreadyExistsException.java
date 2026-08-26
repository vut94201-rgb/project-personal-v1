package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;

public class CrewCodeAlreadyExistsException extends RuntimeException {
    public CrewCodeAlreadyExistsException(DepartmentId departmentId, String code) {
        super("Crew code already exists in department " + departmentId.value() + ": " + code);
    }
}