package com.hanyang.identity.identityservicev4mono.security.revocation;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface AccessRevocationStore {

    void revokeSession(String sessionId, Duration ttl);

    boolean isSessionRevoked(String sessionId);
}