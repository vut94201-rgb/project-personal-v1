package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

public class ServicePrincipalDisabledException extends RuntimeException {

    public ServicePrincipalDisabledException(ServicePrincipalId id) {
        super("Service principal is disabled: " + id.value());
    }
}