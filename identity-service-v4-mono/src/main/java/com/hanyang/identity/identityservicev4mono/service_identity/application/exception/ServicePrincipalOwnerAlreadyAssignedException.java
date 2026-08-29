package com.hanyang.identity.identityservicev4mono.service_identity.application.exception;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

public class ServicePrincipalOwnerAlreadyAssignedException extends RuntimeException {

    public ServicePrincipalOwnerAlreadyAssignedException(
            ServicePrincipalId servicePrincipalId,
            EmployeeId employeeId
    ) {
        super(
                "Employee is already an active owner of service principal. servicePrincipalId=%s, employeeId=%s"
                        .formatted(servicePrincipalId.value(), employeeId.value())
        );
    }
}