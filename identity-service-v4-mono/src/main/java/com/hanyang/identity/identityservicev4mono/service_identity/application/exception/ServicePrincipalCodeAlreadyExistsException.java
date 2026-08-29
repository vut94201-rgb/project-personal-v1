package com.hanyang.identity.identityservicev4mono.service_identity.application.exception;

public class ServicePrincipalCodeAlreadyExistsException extends RuntimeException {

    public ServicePrincipalCodeAlreadyExistsException(String code) {
        super("Service principal code already exists: " + code);
    }
}