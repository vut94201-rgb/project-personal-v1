package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

public class ServicePrincipalRoleAlreadyAssignedException extends RuntimeException {

    public ServicePrincipalRoleAlreadyAssignedException(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        super(
                "Role " + roleId.value()
                        + " is already assigned to service principal "
                        + servicePrincipalId.value()
        );
    }
}