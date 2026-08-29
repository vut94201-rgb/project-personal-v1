package com.hanyang.identity.identityservicev4mono.service_identity.application.exception;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

public class ServicePrincipalPrimaryOwnerAlreadyExistsException extends RuntimeException {

    public ServicePrincipalPrimaryOwnerAlreadyExistsException(
            ServicePrincipalId servicePrincipalId
    ) {
        super(
                "Service principal already has an active PRIMARY owner: "
                        + servicePrincipalId.value()
        );
    }
}