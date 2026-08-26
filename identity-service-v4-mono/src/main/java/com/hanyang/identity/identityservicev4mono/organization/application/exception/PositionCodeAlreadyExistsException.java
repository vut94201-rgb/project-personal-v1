package com.hanyang.identity.identityservicev4mono.organization.application.exception;

public class PositionCodeAlreadyExistsException extends RuntimeException {
    public PositionCodeAlreadyExistsException(String code) {
        super("Position code already exists: " + code);
    }
}
