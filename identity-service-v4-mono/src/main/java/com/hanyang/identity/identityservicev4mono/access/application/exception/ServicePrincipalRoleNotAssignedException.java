package com.hanyang.identity.identityservicev4mono.access.application.exception;


import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

public class ServicePrincipalRoleNotAssignedException extends RuntimeException {

    public ServicePrincipalRoleNotAssignedException(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        super(
                "Role " + roleId.value()
                        + " is not assigned to service principal "
                        + servicePrincipalId.value()
        );
    }
}