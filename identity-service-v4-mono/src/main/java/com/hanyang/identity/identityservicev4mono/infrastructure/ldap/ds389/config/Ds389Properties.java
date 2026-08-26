package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.ds389")
public record Ds389Properties(
        boolean enabled,
        String url,
        String baseDn,
        String bindDn,
        String bindPassword,
        String peopleOu,
        boolean requireTls
) {
}