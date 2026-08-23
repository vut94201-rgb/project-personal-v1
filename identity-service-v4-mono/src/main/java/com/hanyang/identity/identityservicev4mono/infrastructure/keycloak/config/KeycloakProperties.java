package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.keycloak")
public record KeycloakProperties(
        String serverUrl,
        String realm,
        String adminClientId,
        String adminClientSecret
) {
}