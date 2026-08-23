package com.hanyang.identity.identityservicev4mono.access.application.exception;

public class ApplicationCodeAlreadyExistsException extends RuntimeException {

    public ApplicationCodeAlreadyExistsException(String code) {
        super("Application code already exists: " + code);
    }
}