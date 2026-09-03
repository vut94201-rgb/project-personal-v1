package com.hanyang.identity.identityservicev4mono.security.revocation.backchannel;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class KeycloakLogoutTokenVerifier
        implements LogoutTokenVerifier {

    private static final String BACKCHANNEL_LOGOUT_EVENT =
            "http://schemas.openid.net/event/backchannel-logout";

    private final JwtDecoder jwtDecoder;
    private final BackChannelLogoutProperties properties;

    public KeycloakLogoutTokenVerifier(
            @Qualifier("backChannelLogoutJwtDecoder")
            JwtDecoder jwtDecoder,
            BackChannelLogoutProperties properties
    ) {
        this.jwtDecoder = jwtDecoder;
        this.properties = properties;
    }

    @Override
    public VerifiedLogoutToken verify(String logoutToken) {
        if (logoutToken == null || logoutToken.isBlank()) {
            throw new BadJwtException(
                    "logout_token must not be blank"
            );
        }

        Jwt jwt = jwtDecoder.decode(logoutToken);

        validateAudience(jwt);
        validateEvents(jwt);
        validateNonce(jwt);

        String sid = jwt.getClaimAsString("sid");

        if (sid == null || sid.isBlank()) {
            throw new BadJwtException(
                    "Logout token does not contain sid"
            );
        }

        return new VerifiedLogoutToken(sid);
    }

    private void validateAudience(Jwt jwt) {
        boolean accepted = Objects.requireNonNull(jwt.getAudience())
                .stream()
                .anyMatch(properties.allowedAudiences()::contains);

        if (!accepted) {
            throw new BadJwtException(
                    "Invalid logout token audience"
            );
        }
    }

    private void validateEvents(Jwt jwt) {
        Object rawEvents = jwt.getClaims().get("events");

        if (!(rawEvents instanceof Map<?, ?> events)
                || !events.containsKey(BACKCHANNEL_LOGOUT_EVENT)) {
            throw new BadJwtException(
                    "Invalid backchannel logout events claim"
            );
        }
    }

    private void validateNonce(Jwt jwt) {
        if (jwt.getClaims().containsKey("nonce")) {
            throw new BadJwtException(
                    "Logout token must not contain nonce"
            );
        }
    }
}