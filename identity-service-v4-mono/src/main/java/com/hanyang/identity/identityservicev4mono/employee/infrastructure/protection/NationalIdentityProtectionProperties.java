package com.hanyang.identity.identityservicev4mono.employee.infrastructure.protection;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "employee.national-identity.protection")
public record NationalIdentityProtectionProperties(
        String encryptionKey,
        String fingerprintKey
) {
}