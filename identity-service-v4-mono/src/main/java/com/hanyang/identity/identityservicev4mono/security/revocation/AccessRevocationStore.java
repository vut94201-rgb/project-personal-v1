package com.hanyang.identity.identityservicev4mono.security.revocation;

import java.time.Instant;
import java.util.Optional;

public interface AccessRevocationStore {

    void revokeUserBefore(String subject, Instant revokedAt);

    Optional<Instant> findUserRevokedBefore(String subject);

    void revokeSession(String sessionId, Instant revokedAt);

    Optional<Instant> findSessionRevokedBefore(String sessionId);
}