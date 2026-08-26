package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.security;


import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakLdapFederationProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config.Ds389Properties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Fails fast when production-style TLS enforcement is requested but either
 * Identity Service or Keycloak is still configured to reach 389 DS over plain
 * LDAP. Local development can keep the flags disabled while production turns
 * them on explicitly.
 */
@Component
@RequiredArgsConstructor
public class LdapTransportSecurityValidator {

    private final Ds389Properties ds389Properties;
    private final KeycloakLdapFederationProperties federationProperties;

    @PostConstruct
    void validate() {
        if (ds389Properties.enabled() && ds389Properties.requireTls()) {
            requireLdaps(
                    ds389Properties.url(),
                    "integration.ds389.url"
            );
        }

        if (federationProperties.enabled() && federationProperties.requireTls()) {
            requireLdaps(
                    federationProperties.connectionUrl(),
                    "integration.keycloak.ldap-federation.connection-url"
            );
        }
    }

    private static void requireLdaps(String value, String propertyName) {
        if (value == null
                || !value.trim().toLowerCase(Locale.ROOT).startsWith("ldaps://")) {
            throw new IllegalStateException(
                    propertyName + " must use ldaps:// when TLS is required"
            );
        }
    }
}