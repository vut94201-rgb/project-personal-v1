package com.hanyang.identity.identityservicev4mono.service_identity.application.exception;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

public class ServicePrincipalNotFoundException extends RuntimeException {

    public ServicePrincipalNotFoundException(ServicePrincipalId id) {
        super("Service principal not found: " + id.value());
    }

    public ServicePrincipalNotFoundException(String code) {
        super("Service principal not found by code: " + code);
    }
}