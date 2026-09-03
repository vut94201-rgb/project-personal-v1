package com.hanyang.identity.identityservicev4mono.security.revocation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevokedSessionJwtValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error REVOKED_SESSION_ERROR =
            new OAuth2Error("invalid_token", "Keycloak session has been revoked", null);

    private final AccessRevocationStore revocationStore;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String sessionId = jwt.getClaimAsString("sid");


        if (sessionId == null || sessionId.isBlank()) {
            return OAuth2TokenValidatorResult.success();
        }

        if (revocationStore.isSessionRevoked(sessionId)) {
            return OAuth2TokenValidatorResult.failure(REVOKED_SESSION_ERROR);
        }

        return OAuth2TokenValidatorResult.success();
    }
}