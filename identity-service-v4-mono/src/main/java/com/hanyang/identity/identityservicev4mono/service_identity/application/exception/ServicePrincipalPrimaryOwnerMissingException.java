package com.hanyang.identity.identityservicev4mono.service_identity.application.exception;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

public class ServicePrincipalPrimaryOwnerMissingException extends RuntimeException {

    public ServicePrincipalPrimaryOwnerMissingException(
            ServicePrincipalId servicePrincipalId
    ) {
        super(
                "Service principal has no active PRIMARY owner: "
                        + servicePrincipalId.value()
        );
    }
}