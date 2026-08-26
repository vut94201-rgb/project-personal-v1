package com.hanyang.identity.identityservicev4mono.security;

import com.hanyang.identity.identityservicev4mono.security.keycloak.KeycloakClientRoleGrantedAuthoritiesConverter;
import com.hanyang.identity.identityservicev4mono.security.keycloak.KeycloakResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@Profile("keycloak")
@EnableMethodSecurity
@EnableConfigurationProperties(KeycloakResourceServerProperties.class)
public class KeycloakResourceServerConfiguration {

  @Bean
  JwtAuthenticationConverter keycloakJwtAuthenticationConverter(
          KeycloakResourceServerProperties properties) {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
            new KeycloakClientRoleGrantedAuthoritiesConverter(properties.resourceClientId()));
    return converter;
  }

  @Bean
  SecurityFilterChain keycloakSecurityFilterChain(
          HttpSecurity http, JwtAuthenticationConverter keycloakJwtAuthenticationConverter)
          throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(
                    authorize ->
                            authorize
                                    .requestMatchers("/actuator/health", "/actuator/info")
                                    .permitAll()
                                    .anyRequest()
                                    .authenticated()
            )
            .oauth2ResourceServer(
                    oauth2 ->
                            oauth2.jwt(
                                    jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)));

    return http.build();
  }
}