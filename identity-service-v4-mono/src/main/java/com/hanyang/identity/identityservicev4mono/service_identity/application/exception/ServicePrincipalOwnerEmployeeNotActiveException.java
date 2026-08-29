package com.hanyang.identity.identityservicev4mono.service_identity.application.exception;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;

public class ServicePrincipalOwnerEmployeeNotActiveException extends RuntimeException {

    public ServicePrincipalOwnerEmployeeNotActiveException(
            EmployeeId employeeId,
            EmployeeStatus status
    ) {
        super(
                "Service principal owner must be an ACTIVE employee. employeeId=%s, status=%s"
                        .formatted(employeeId.value(), status)
        );
    }
}