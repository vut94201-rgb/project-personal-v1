package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception;

public class KeycloakIntegrationException extends RuntimeException {

            public KeycloakIntegrationException(String message) {
                super(message);
            }

            public KeycloakIntegrationException(
            String message,
            Throwable cause
   ) {
                super(message, cause);
            }
}