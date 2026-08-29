package com.hanyang.identity.identityservicev4mono.service_identity.application.command;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnershipType;

import java.util.Objects;

public record AssignServicePrincipalOwnerCommand(
        ServicePrincipalId servicePrincipalId,
        EmployeeId employeeId,
        ServicePrincipalOwnershipType ownershipType
) {
    public AssignServicePrincipalOwnerCommand {
        Objects.requireNonNull(servicePrincipalId, "servicePrincipalId must not be null");
        Objects.requireNonNull(employeeId, "employeeId must not be null");
        Objects.requireNonNull(ownershipType, "ownershipType must not be null");
    }
}