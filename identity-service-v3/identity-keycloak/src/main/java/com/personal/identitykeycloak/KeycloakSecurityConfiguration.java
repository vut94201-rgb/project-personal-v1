package com.personal.identitykeycloak;

import com.personal.identitykeycloak.internal.security.KeycloakClientRolesConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
@Configuration
public class KeycloakSecurityConfiguration   {


    @Bean
    JwtAuthenticationConverter keycloakJwtAuthenticationConverter(
            @Value(
                    "${identity.keycloak.resource-client-id:identity-api}"
            )
            String resourceClientId
    ) {
        var authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                new KeycloakClientRolesConverter(
                        resourceClientId
                )
        );

        return authenticationConverter;
    }


}
