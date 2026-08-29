package com.hanyang.identity.identityservicev4mono.service_identity.application.exception;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerId;

public class ServicePrincipalOwnerNotFoundException extends RuntimeException {

    public ServicePrincipalOwnerNotFoundException(ServicePrincipalOwnerId id) {
        super("Service principal owner assignment not found: " + id.value());
    }
}