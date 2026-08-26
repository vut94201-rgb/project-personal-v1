package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;

public class DepartmentHasActiveCrewsException extends RuntimeException {
    public DepartmentHasActiveCrewsException(DepartmentId departmentId) {
        super("Department has active crews: " + departmentId.value());
    }
}