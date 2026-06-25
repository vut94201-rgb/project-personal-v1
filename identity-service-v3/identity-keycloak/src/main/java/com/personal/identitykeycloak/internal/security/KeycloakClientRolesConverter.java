package com.personal.identitykeycloak.internal.security;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

public final class KeycloakClientRolesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String RESOURCE_ACCESS_CLAIM =
            "resource_access";

    private static final String ROLES_CLAIM =
            "roles";

    private final String resourceClientId;

    private final JwtGrantedAuthoritiesConverter scopeConverter =
            new JwtGrantedAuthoritiesConverter();

    public KeycloakClientRolesConverter(
            String resourceClientId
    ) {
        this.resourceClientId = resourceClientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        var authorities =
                new LinkedHashSet<GrantedAuthority>();

        // Giữ lại các authority mặc định như SCOPE_email,
        // SCOPE_profile...
        var scopeAuthorities = scopeConverter.convert(jwt);

        if (Objects.nonNull(scopeAuthorities)) {
            authorities.addAll(scopeAuthorities);
        }

        Map<String, Object> resourceAccess =
                jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM);

        if (resourceAccess == null) {
            return authorities;
        }

        Object clientAccess =
                resourceAccess.get(resourceClientId);

        if (!(clientAccess instanceof Map<?, ?> clientAccessMap)) {
            return authorities;
        }

        Object roles = clientAccessMap.get(ROLES_CLAIM);

        if (!(roles instanceof Collection<?> roleCollection)) {
            return authorities;
        }

        roleCollection.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        return authorities;
    }
}
