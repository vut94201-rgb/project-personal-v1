package com.hanyang.identity.identityservicev4mono.playground;

import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Profile("dev")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DevApplicationRunner implements ApplicationRunner {
    Keycloak keycloakAdminClient;
    ProfileChecker profileChecker;
    KeycloakProperties properties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {

            log.info("Keycloak URL = {}", properties.serverUrl());
            log.info("Keycloak realm = {}", properties.realm());
            log.info("Keycloak client = {}", properties.adminClientId());
            log.info(
                    "Keycloak secret present = {}, length = {}",
                    properties.adminClientSecret() != null
                            && !properties.adminClientSecret().isBlank(),
                    properties.adminClientSecret() == null
                            ? 0
                            : properties.adminClientSecret().length()
            );
            var realms = keycloakAdminClient.realms().findAll();

            realms.forEach(realm ->
                    log.info("realm = {}", realm.getRealm())
            );

        } catch (Exception e) {
            log.error("Keycloak playground test failed", e);
        }
    }

}
