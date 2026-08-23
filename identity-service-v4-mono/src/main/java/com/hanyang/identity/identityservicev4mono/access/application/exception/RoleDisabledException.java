package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;

public class RoleDisabledException extends RuntimeException {

            public RoleDisabledException(RoleId id) {
                super("Role is disabled: " + id.value());
            }
}