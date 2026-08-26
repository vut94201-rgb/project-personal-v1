package com.hanyang.identity.identityservicev4mono.shared.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxEventSnapshot(
        UUID id,
        String aggregateType,
        String aggregateId,
        String eventType,
        OutboxEventStatus status,
        int attemptCount,
        Instant availableAt,
        Instant processingStartedAt,
        Instant processedAt,
        String lastError,
        Instant createdAt,
        Instant updatedAt
) {
}