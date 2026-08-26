package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.keycloak.ldap-federation")
public record KeycloakLdapFederationProperties(
        boolean enabled,
        String name,
        String connectionUrl,
        String editMode
) {
}