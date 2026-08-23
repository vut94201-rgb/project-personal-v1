package com.hanyang.identity.identityservicev4mono.shared.outbox;

import java.util.Objects;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        int attemptCount
) {
    public OutboxEvent {
            Objects.requireNonNull(id, "id must not be null");
                aggregateType = requireText(aggregateType, "aggregateType");        aggregateId = requireText(aggregateId, "aggregateId");
                eventType = requireText(eventType, "eventType");

                        if (attemptCount < 1) {
                        throw new IllegalArgumentException("attemptCount must be at least 1 for a claimed event");
                   }
            }

            private static String requireText(String value, String fieldName) {
                Objects.requireNonNull(value, fieldName + " must not be null");

                String normalized = value.trim();
               if (normalized.isEmpty()) {
                        throw new IllegalArgumentException(fieldName + " must not be blank");
                    }
                return normalized;
           }
}