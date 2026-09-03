package com.hanyang.identity.identityservicev4mono.security.revocation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.access-revocation")
public record AccessRevocationProperties(
        Duration sessionRetention,
        String keyPrefix
) {

    public AccessRevocationProperties {
        if (sessionRetention == null
                || sessionRetention.isZero()
                || sessionRetention.isNegative()) {
            throw new IllegalArgumentException(
                    "security.access-revocation.session-retention must be positive"
            );
        }

        if (keyPrefix == null || keyPrefix.isBlank()) {
            throw new IllegalArgumentException(
                    "security.access-revocation.key-prefix must not be blank"
            );
        }
    }
}