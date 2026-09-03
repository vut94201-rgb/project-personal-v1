package com.hanyang.identity.identityservicev4mono.security.revocation.backchannel;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "security.oidc.backchannel-logout")
public record BackChannelLogoutProperties(
        String issuer,
        Set<String> allowedAudiences
) {

    public BackChannelLogoutProperties {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException(
                    "security.oidc.backchannel-logout.issuer must not be blank"
            );
        }

        if (allowedAudiences == null || allowedAudiences.isEmpty()) {
            throw new IllegalArgumentException(
                    "security.oidc.backchannel-logout.allowed-audiences must not be empty"
            );
        }
    }
}