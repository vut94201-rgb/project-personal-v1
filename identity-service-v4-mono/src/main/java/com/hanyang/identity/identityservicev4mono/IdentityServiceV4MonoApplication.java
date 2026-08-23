package com.hanyang.identity.identityservicev4mono;

import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication

public class IdentityServiceV4MonoApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceV4MonoApplication.class, args);
    }

}
