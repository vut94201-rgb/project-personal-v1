package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;

public class PermissionCodeAlreadyExistsException extends RuntimeException {

    public PermissionCodeAlreadyExistsException(
            ApplicationId applicationId,
            String code
    ) {
        super(
                "Permission code already exists in application %s: %s"
                        .formatted(applicationId.value(), code)
        );
    }
}