package com.hanyang.identity.identityservicev4mono.security.keycloak;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.keycloak")
public record KeycloakResourceServerProperties(
        String resourceClientId
) {
}