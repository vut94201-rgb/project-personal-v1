package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;

public class PositionNotFoundException extends RuntimeException {
    public PositionNotFoundException(PositionId id) {
        super("Position not found: " + id.value());
    }

    public PositionNotFoundException(String code) {
        super("Position not found: " + code);
    }
}
