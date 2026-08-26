package com.hanyang.identity.identityservicev4mono.security.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakClientRoleGrantedAuthoritiesConverterTest {

    private final KeycloakClientRoleGrantedAuthoritiesConverter converter =
            new KeycloakClientRoleGrantedAuthoritiesConverter("identity");

    @Test
    void mapsOnlyConfiguredClientRoles() {
        Jwt jwt = jwt(Map.of(
                "identity", Map.of("roles", List.of("IDENTITY_ADMIN", "IDENTITY_VIEWER")),
                "oqc", Map.of("roles", List.of("OQC_OPERATOR"))
        ));

        Set<String> authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertEquals(
                Set.of("ROLE_IDENTITY_ADMIN", "ROLE_IDENTITY_VIEWER"),
                authorities
        );
    }

    @Test
    void ignoresMissingConfiguredClient() {
        Jwt jwt = jwt(Map.of(
                "oqc", Map.of("roles", List.of("OQC_OPERATOR"))
        ));

        assertTrue(converter.convert(jwt).isEmpty());
    }

    @Test
    void ignoresMalformedResourceAccessClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("resource_access", "not-a-map")
                .build();

        assertTrue(converter.convert(jwt).isEmpty());
    }

    @Test
    void ignoresBlankAndNonStringRolesAndDeduplicatesAuthorities() {
        Jwt jwt = jwt(Map.of(
                "identity", Map.of(
                        "roles",
                        List.of("IDENTITY_ADMIN", "", 123, "IDENTITY_ADMIN")
                )
        ));

        Set<String> authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertEquals(Set.of("ROLE_IDENTITY_ADMIN"), authorities);
    }

    private static Jwt jwt(Map<String, Object> resourceAccess) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("resource_access", resourceAccess)
                .build();
    }
}