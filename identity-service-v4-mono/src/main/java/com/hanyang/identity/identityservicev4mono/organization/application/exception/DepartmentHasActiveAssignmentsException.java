package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;

public class DepartmentHasActiveAssignmentsException extends RuntimeException {
    public DepartmentHasActiveAssignmentsException(DepartmentId id) {
        super("Department still has active organizational assignments: " + id.value());
    }
}
