package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({KeycloakProperties.class, KeycloakLdapFederationProperties.class})
public class KeycloakAdminClientConfiguration {

  @Bean(destroyMethod = "close")
  public Keycloak keycloakAdminClient(KeycloakProperties properties) {
    return KeycloakBuilder.builder()
        .serverUrl(requireText(properties.serverUrl(), "integration.keycloak.server-url"))
        .realm(requireText(properties.realm(), "integration.keycloak.realm"))
        .clientId(requireText(properties.adminClientId(), "integration.keycloak.admin-client-id"))
        .clientSecret(normalizeSecret(properties.adminClientSecret()))
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .build();
  }

  private static String normalizeSecret(String value) {
    return value == null ? "" : value.trim();
  }

  private static String requireText(String value, String propertyName) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(propertyName + " must not be blank");
    }
    return value.trim();
  }
}
