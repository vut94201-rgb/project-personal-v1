package com.hanyang.identity.identityservicev4mono.account.application.exception;

public class UsernameAlreadyExistsException
        extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }
}