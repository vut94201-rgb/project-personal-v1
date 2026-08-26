package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

public class EmployeeAlreadyHasActiveOrganizationalAssignmentException extends RuntimeException {
    public EmployeeAlreadyHasActiveOrganizationalAssignmentException(EmployeeId employeeId) {
        super("Employee already has an active organizational assignment: " + employeeId.value());
    }
}
