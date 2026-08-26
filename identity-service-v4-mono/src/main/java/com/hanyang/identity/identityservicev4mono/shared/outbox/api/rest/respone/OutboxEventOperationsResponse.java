package com.hanyang.identity.identityservicev4mono.shared.outbox.api.rest.respone;


import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventSnapshot;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventStatus;

import java.time.Instant;
import java.util.UUID;

public record OutboxEventOperationsResponse(
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

    public static OutboxEventOperationsResponse from(OutboxEventSnapshot event) {
        return new OutboxEventOperationsResponse(
                event.id(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                event.status(),
                event.attemptCount(),
                event.availableAt(),
                event.processingStartedAt(),
                event.processedAt(),
                event.lastError(),
                event.createdAt(),
                event.updatedAt()
        );
    }
}