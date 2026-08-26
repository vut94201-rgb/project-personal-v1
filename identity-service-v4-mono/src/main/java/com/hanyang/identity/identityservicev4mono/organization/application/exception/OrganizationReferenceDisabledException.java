package com.hanyang.identity.identityservicev4mono.organization.application.exception;

public class OrganizationReferenceDisabledException extends RuntimeException {
    public OrganizationReferenceDisabledException(String referenceType, Object id) {
        super(referenceType + " is disabled and cannot be used for an active assignment: " + id);
    }
}
