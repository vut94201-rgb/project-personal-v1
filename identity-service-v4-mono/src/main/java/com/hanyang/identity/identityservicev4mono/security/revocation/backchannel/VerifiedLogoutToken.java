package com.hanyang.identity.identityservicev4mono.security.revocation.backchannel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public record VerifiedLogoutToken(
        String sessionId
) {
}