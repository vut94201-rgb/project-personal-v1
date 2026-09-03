package com.hanyang.identity.identityservicev4mono.security;

import com.hanyang.identity.identityservicev4mono.security.keycloak.KeycloakClientRoleGrantedAuthoritiesConverter;
import com.hanyang.identity.identityservicev4mono.security.keycloak.KeycloakResourceServerProperties;
import com.hanyang.identity.identityservicev4mono.security.revocation.RevokedSessionJwtValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Objects;

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

  @Bean("accessTokenJwtDecoder")
  JwtDecoder accessTokenJwtDecoder(
          @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
          KeycloakResourceServerProperties properties,
          RevokedSessionJwtValidator revokedSessionJwtValidator) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

    OAuth2TokenValidator<Jwt> defaultValidator =
            JwtValidators.createDefaultWithIssuer(issuerUri);

    OAuth2TokenValidator<Jwt> audienceValidator =
            jwt -> {
              if (Objects.requireNonNull(jwt.getAudience()).contains(properties.resourceClientId())) {
                return OAuth2TokenValidatorResult.success();
              }

              return OAuth2TokenValidatorResult.failure(
                      new OAuth2Error(
                              "invalid_token", "Access token does not contain the required audience", null));
            };

    decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                    defaultValidator, audienceValidator, revokedSessionJwtValidator));

    return decoder;
  }

  @Bean
  SecurityFilterChain keycloakSecurityFilterChain(
          HttpSecurity http,
          JwtAuthenticationConverter keycloakJwtAuthenticationConverter,
          @Qualifier("accessTokenJwtDecoder") JwtDecoder accessTokenJwtDecoder)
          throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(
                    authorize ->
                            authorize
                                    .requestMatchers(HttpMethod.POST, "/internal/oidc/backchannel-logout")
                                    .permitAll()
                                    .requestMatchers("/actuator/health", "/actuator/info")
                                    .permitAll()
                                    .anyRequest()
                                    .authenticated())
            .oauth2ResourceServer(
                    oauth2 ->
                            oauth2.jwt(
                                    jwt ->
                                            jwt.decoder(accessTokenJwtDecoder)
                                                    .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)));

    return http.build();
  }
}