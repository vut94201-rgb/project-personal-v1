package com.hanyang.identity.identityservicev4mono.access.application.exception;


import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;

public class RoleCodeAlreadyExistsException extends RuntimeException {

    public RoleCodeAlreadyExistsException(
            ApplicationId applicationId,
            String code
    ) {
        super(
                "Role code already exists in application %s: %s"
                        .formatted(applicationId.value(), code)
        );
    }
}