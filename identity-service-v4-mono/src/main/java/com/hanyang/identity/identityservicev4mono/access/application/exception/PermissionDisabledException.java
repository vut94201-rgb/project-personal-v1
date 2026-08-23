package com.hanyang.identity.identityservicev4mono.access.application.exception;


import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;

public class PermissionDisabledException extends RuntimeException {

            public PermissionDisabledException(PermissionId id) {
                super("Permission is disabled: " + id.value());
            }
}