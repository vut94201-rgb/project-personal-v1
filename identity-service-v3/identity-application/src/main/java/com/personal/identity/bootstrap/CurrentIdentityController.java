package com.personal.identity.bootstrap;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/me")
public class CurrentIdentityController {

    @GetMapping
    @PreAuthorize("hasAuthority('account.read')")
    public CurrentIdentityResponse currentIdentity(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return new CurrentIdentityResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                Objects.requireNonNull(jwt.getIssuer()).toString(),
                jwt.getIssuedAt(),
                jwt.getExpiresAt()
        );
    }

    public record CurrentIdentityResponse(
            String subject,
            String username,
            String email,
            String issuer,
            Instant issuedAt,
            Instant expiresAt
    ) {
    }
}