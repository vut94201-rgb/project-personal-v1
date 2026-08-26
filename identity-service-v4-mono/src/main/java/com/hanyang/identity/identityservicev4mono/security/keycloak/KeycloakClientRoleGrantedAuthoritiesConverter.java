package com.hanyang.identity.identityservicev4mono.security.keycloak;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class KeycloakClientRoleGrantedAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String RESOURCE_ACCESS = "resource_access";
    private static final String ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    private final String clientId;

    public KeycloakClientRoleGrantedAuthoritiesConverter(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Keycloak resource client id must not be blank");
        }
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object resourceAccessClaim = jwt.getClaims().get(RESOURCE_ACCESS);
        if (!(resourceAccessClaim instanceof Map<?, ?> resourceAccess)) {
            return Set.of();
        }

        Object clientAccessClaim = resourceAccess.get(clientId);
        if (!(clientAccessClaim instanceof Map<?, ?> clientAccess)) {
            return Set.of();
        }

        Object rolesClaim = clientAccess.get(ROLES);
        if (!(rolesClaim instanceof Collection<?> roles)) {
            return Set.of();
        }

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (Object role : roles) {
            if (role instanceof String roleName && !roleName.isBlank()) {
                authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + roleName));
            }
        }
        return Set.copyOf(authorities);
    }
}