package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(RoleId id) {
        super("Role not found: " + id.value());
    }
}