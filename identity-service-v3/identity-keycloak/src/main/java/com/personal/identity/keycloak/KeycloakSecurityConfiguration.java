package com.personal.identity.keycloak;

import com.personal.identity.keycloak.security.KeycloakClientRolesConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration(proxyBeanMethods = false)
public class KeycloakSecurityConfiguration {

    @Bean
    public JwtAuthenticationConverter keycloakJwtAuthenticationConverter(
            @Value("${identity.keycloak.resource-client-id:identity-api}")
            String resourceClientId
    ) {
        var converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                new KeycloakClientRolesConverter(resourceClientId)
        );

        return converter;
    }
}