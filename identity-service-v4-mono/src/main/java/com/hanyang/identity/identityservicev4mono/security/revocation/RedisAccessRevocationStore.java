package com.hanyang.identity.identityservicev4mono.security.revocation;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class RedisAccessRevocationStore
        implements AccessRevocationStore {

    private static final String SESSION = "session";
    private static final String REVOKED_VALUE = "1";

    private final StringRedisTemplate redisTemplate;
    private final AccessRevocationProperties properties;

    public RedisAccessRevocationStore(
            StringRedisTemplate redisTemplate,
            AccessRevocationProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public void revokeSession(
            String sessionId,
            Duration ttl
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "sessionId must not be blank"
            );
        }

        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException(
                    "ttl must be positive"
            );
        }

        redisTemplate.opsForValue().set(
                sessionKey(sessionId),
                REVOKED_VALUE,
                ttl
        );
    }

    @Override
    public boolean isSessionRevoked(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(sessionKey(sessionId))
        );
    }

    private String sessionKey(String sessionId) {
        return properties.keyPrefix()
                + ":" + SESSION
                + ":" + sessionId;
    }
}