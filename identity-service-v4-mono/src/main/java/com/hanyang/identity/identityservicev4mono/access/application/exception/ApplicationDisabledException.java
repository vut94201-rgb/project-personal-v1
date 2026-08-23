package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;

public class ApplicationDisabledException extends RuntimeException {

    public ApplicationDisabledException(ApplicationId id) {
        super("Application is disabled: " + id.value());
    }
}