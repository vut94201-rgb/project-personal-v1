package com.hanyang.identity.identityservicev4mono.service_identity.application.exception;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerId;

public class ServicePrincipalPrimaryOwnerRevocationNotAllowedException extends RuntimeException {

    public ServicePrincipalPrimaryOwnerRevocationNotAllowedException(
            ServicePrincipalOwnerId ownerId
    ) {
        super(
                "PRIMARY service principal owner cannot be revoked directly; "
                        + "transfer PRIMARY ownership instead. ownerId="
                        + ownerId.value()
        );
    }
}