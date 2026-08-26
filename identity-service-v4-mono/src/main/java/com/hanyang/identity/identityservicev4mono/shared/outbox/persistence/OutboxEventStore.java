package com.hanyang.identity.identityservicev4mono.shared.outbox.persistence;


import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventSnapshot;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventStatus;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxWorkerProperties;
import com.hanyang.identity.identityservicev4mono.shared.outbox.exception.OutboxEventNotFoundException;
import com.hanyang.identity.identityservicev4mono.shared.outbox.exception.OutboxEventRetryNotAllowedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxEventStore {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final OutboxEventJpaRepository jpaRepository;
    private final OutboxWorkerProperties properties;
    private final Clock clock;
    // Add new event
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID append(
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload
    ) {
        Instant now = clock.instant();

        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setAggregateType(requireText(aggregateType, "aggregateType"));
        entity.setAggregateId(requireText(aggregateId, "aggregateId"));
        entity.setEventType(requireText(eventType, "eventType"));
        entity.setPayload(normalizeNullable(payload));
        entity.setStatus(OutboxEventStatus.PENDING);
        entity.setAttemptCount(0);
        entity.setAvailableAt(now);
        entity.setProcessingStartedAt(null);
        entity.setProcessedAt(null);
        entity.setLastError(null);

        return jpaRepository.save(entity).getId();
    }
    // make processing
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OutboxEvent> claimBatch() {
        Instant now = clock.instant();
        Instant staleBefore = now.minus(properties.getProcessingTimeout());

        List<OutboxEventJpaEntity> candidates = jpaRepository.lockClaimableEvents(
                now,
                staleBefore,
                properties.getBatchSize()
        );

        List<OutboxEvent> claimed = new ArrayList<>(candidates.size());

        for (OutboxEventJpaEntity entity : candidates) {
            if (entity.getAttemptCount() >= properties.getMaxAttempts()) {
                entity.setStatus(OutboxEventStatus.DEAD);
                entity.setProcessingStartedAt(null);
                entity.setLastError(normalizeError(
                        entity.getLastError() == null
                                ? "Maximum outbox delivery attempts exceeded"
                                : entity.getLastError()
                ));
                continue;
            }

            entity.setStatus(OutboxEventStatus.PROCESSING);
            entity.setProcessingStartedAt(now);
            entity.setAttemptCount(Math.incrementExact(entity.getAttemptCount()));

            claimed.add(toClaimedEvent(entity));
        }

        jpaRepository.saveAll(candidates);
        return claimed;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markProcessed(UUID eventId, int claimedAttemptCount) {
        OutboxEventJpaEntity entity = requiredEntity(eventId);

        if (!isCurrentProcessingAttempt(entity, claimedAttemptCount)) {
            return false;
        }

        entity.setStatus(OutboxEventStatus.PROCESSED);
        entity.setProcessedAt(clock.instant());
        entity.setProcessingStartedAt(null);
        entity.setLastError(null);
        jpaRepository.save(entity);
        return true;
    }

    @Transactional(readOnly = true)
    public List<OutboxEventSnapshot> findDeadEvents(int limit) {
        if (limit < 1 || limit > 500) {
            throw new IllegalArgumentException("limit must be between 1 and 500");
        }

        return jpaRepository.findByStatusOrderByUpdatedAtDesc(
                        OutboxEventStatus.DEAD,
                        PageRequest.of(0, limit)
                ).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboxEventSnapshot retryDead(UUID eventId) {
        OutboxEventJpaEntity entity = jpaRepository.lockById(eventId)
                .orElseThrow(() -> new OutboxEventNotFoundException(eventId));

        if (entity.getStatus() != OutboxEventStatus.DEAD) {
            throw new OutboxEventRetryNotAllowedException(
                    eventId,
                    entity.getStatus()
            );
        }

        entity.setStatus(OutboxEventStatus.PENDING);
        entity.setAttemptCount(0);
        entity.setAvailableAt(clock.instant());
        entity.setProcessingStartedAt(null);
        entity.setProcessedAt(null);

        return toSnapshot(jpaRepository.save(entity));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markFailed(UUID eventId, int claimedAttemptCount, String error) {
        OutboxEventJpaEntity entity = requiredEntity(eventId);

        if (!isCurrentProcessingAttempt(entity, claimedAttemptCount)) {
            return false;
        }

        String normalizedError = normalizeError(error);
        entity.setLastError(normalizedError);
        entity.setProcessingStartedAt(null);

        if (entity.getAttemptCount() >= properties.getMaxAttempts()) {
            entity.setStatus(OutboxEventStatus.DEAD);
            jpaRepository.save(entity);
            return true;
        }

        entity.setStatus(OutboxEventStatus.PENDING);
        entity.setAvailableAt(
                clock.instant().plus(backoffForAttempt(entity.getAttemptCount()))
        );
        jpaRepository.save(entity);
        return true;
    }

    private static boolean isCurrentProcessingAttempt(
            OutboxEventJpaEntity entity,
            int claimedAttemptCount
    ) {
        return entity.getStatus() == OutboxEventStatus.PROCESSING
                && entity.getAttemptCount() == claimedAttemptCount;
    }

    private OutboxEventJpaEntity requiredEntity(UUID eventId) {
        return jpaRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox event not found: " + eventId
                ));
    }

    private OutboxEvent toClaimedEvent(OutboxEventJpaEntity entity) {
        return new OutboxEvent(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getAttemptCount()
        );
    }

    private OutboxEventSnapshot toSnapshot(OutboxEventJpaEntity entity) {
        return new OutboxEventSnapshot(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getStatus(),
                entity.getAttemptCount(),
                entity.getAvailableAt(),
                entity.getProcessingStartedAt(),
                entity.getProcessedAt(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private Duration backoffForAttempt(int attemptCount) {
        Duration backoff = properties.getInitialBackoff();
        Duration maxBackoff = properties.getMaxBackoff();

        for (int attempt = 1; attempt < attemptCount; attempt++) {
            if (backoff.compareTo(maxBackoff) >= 0) {
                return maxBackoff;
            }

            try {
                backoff = backoff.multipliedBy(2);
            } catch (ArithmeticException exception) {
                return maxBackoff;
            }
        }

        return backoff.compareTo(maxBackoff) > 0
                ? maxBackoff
                : backoff;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeError(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null || normalized.length() <= MAX_ERROR_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_ERROR_LENGTH);
    }
}