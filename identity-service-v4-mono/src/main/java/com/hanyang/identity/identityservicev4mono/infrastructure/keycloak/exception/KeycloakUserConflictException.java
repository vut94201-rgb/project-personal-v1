package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception;

public class KeycloakUserConflictException extends RuntimeException {

            public KeycloakUserConflictException(String username) {
                super("Keycloak user already exists for username: " + username);
            }
}