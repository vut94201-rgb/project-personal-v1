package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.security;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakLdapFederationProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config.Ds389Properties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LdapTransportSecurityValidatorTest {

    @Test
    void plainLdapIsRejectedWhenDirectDirectoryTlsIsRequired() {
        LdapTransportSecurityValidator validator = new LdapTransportSecurityValidator(
                new Ds389Properties(
                        true,
                        "ldap://localhost:3389",
                        "dc=hanyang,dc=local",
                        "cn=Directory Manager",
                        "change_me",
                        "ou=People",
                        true
                ),
                new KeycloakLdapFederationProperties(
                        false,
                        "hanyang-389ds",
                        "ldap://host.containers.internal:3389",
                        "WRITABLE",
                        false
                )
        );

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    void ldapsPassesWhenTlsIsRequired() {
        LdapTransportSecurityValidator validator = new LdapTransportSecurityValidator(
                new Ds389Properties(
                        true,
                        "ldaps://localhost:3636",
                        "dc=hanyang,dc=local",
                        "cn=Directory Manager",
                        "change_me",
                        "ou=People",
                        true
                ),
                new KeycloakLdapFederationProperties(
                        true,
                        "hanyang-389ds",
                        "ldaps://host.containers.internal:3636",
                        "WRITABLE",
                        true
                )
        );

        assertDoesNotThrow(validator::validate);
    }
}