package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;

public class PermissionNotFoundException extends RuntimeException {

            public PermissionNotFoundException(PermissionId id) {
                super("Permission not found: " + id.value());
            }
}