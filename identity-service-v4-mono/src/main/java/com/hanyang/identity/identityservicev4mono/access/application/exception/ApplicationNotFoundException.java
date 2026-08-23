package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(ApplicationId id) {
        super("Application not found: " + id.value());
    }

    public ApplicationNotFoundException(String code) {
        super("Application not found: " + code);
    }
}