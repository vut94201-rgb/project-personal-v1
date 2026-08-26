package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.exception;

public class Ds389IntegrationException extends RuntimeException {

    public Ds389IntegrationException(String message) {
        super(message);
    }

    public Ds389IntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}