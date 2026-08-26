package com.hanyang.identity.identityservicev4mono.shared.outbox.persistence;


import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventStatus;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxWorkerProperties;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventStoreTest {

    @Test
    void failedAttemptUsesExponentialBackoffAndEventuallyBecomesDead() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-25T04:00:00Z"));
        InMemoryOutboxRepository repository = new InMemoryOutboxRepository();
        OutboxEventStore store = new OutboxEventStore(
                repository.proxy(),
                properties(2),
                clock
        );

        UUID eventId = append(store);

        OutboxEvent firstAttempt = store.claimBatch().getFirst();
        assertEquals(1, firstAttempt.attemptCount());

        store.markFailed(eventId, firstAttempt.attemptCount(), "keycloak unavailable");

        OutboxEventJpaEntity afterFirstFailure = repository.required(eventId);
        assertEquals(OutboxEventStatus.PENDING, afterFirstFailure.getStatus());
        assertEquals(clock.instant().plusSeconds(2), afterFirstFailure.getAvailableAt());
        assertEquals("keycloak unavailable", afterFirstFailure.getLastError());

        assertTrue(store.claimBatch().isEmpty(), "event must respect its backoff window");

        clock.advance(Duration.ofSeconds(2));
        OutboxEvent secondAttempt = store.claimBatch().getFirst();
        assertEquals(2, secondAttempt.attemptCount());

        store.markFailed(eventId, secondAttempt.attemptCount(), "still unavailable");

        OutboxEventJpaEntity dead = repository.required(eventId);
        assertEquals(OutboxEventStatus.DEAD, dead.getStatus());
        assertEquals(2, dead.getAttemptCount());
        assertEquals("still unavailable", dead.getLastError());
        assertNull(dead.getProcessingStartedAt());
    }

    @Test
    void deadEventCanBeManuallyRetriedWithFreshAttemptBudget() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-25T04:00:00Z"));
        InMemoryOutboxRepository repository = new InMemoryOutboxRepository();
        OutboxEventStore store = new OutboxEventStore(
                repository.proxy(),
                properties(1),
                clock
        );

        UUID eventId = append(store);
        OutboxEvent attempt = store.claimBatch().getFirst();
        store.markFailed(eventId, attempt.attemptCount(), "keycloak unavailable");

        OutboxEventJpaEntity dead = repository.required(eventId);
        assertEquals(OutboxEventStatus.DEAD, dead.getStatus());
        assertEquals(1, dead.getAttemptCount());

        store.retryDead(eventId);

        OutboxEventJpaEntity retried = repository.required(eventId);
        assertEquals(OutboxEventStatus.PENDING, retried.getStatus());
        assertEquals(0, retried.getAttemptCount());
        assertEquals(clock.instant(), retried.getAvailableAt());
        assertNull(retried.getProcessingStartedAt());
        assertNull(retried.getProcessedAt());
        assertEquals("keycloak unavailable", retried.getLastError());

        OutboxEvent retryAttempt = store.claimBatch().getFirst();
        assertEquals(1, retryAttempt.attemptCount());
    }

    @Test
    void staleWorkerCannotCompleteOrFailANewerClaim() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-25T04:00:00Z"));
        InMemoryOutboxRepository repository = new InMemoryOutboxRepository();
        OutboxEventStore store = new OutboxEventStore(
                repository.proxy(),
                properties(4),
                clock
        );

        UUID eventId = append(store);
        OutboxEvent firstAttempt = store.claimBatch().getFirst();
        assertEquals(1, firstAttempt.attemptCount());

        // Simulate worker #1 hanging longer than the processing lease.
        clock.advance(Duration.ofMinutes(6));
        OutboxEvent secondAttempt = store.claimBatch().getFirst();
        assertEquals(2, secondAttempt.attemptCount());

        // Worker #1 wakes up late. It must not mutate worker #2's lease.
        store.markProcessed(eventId, firstAttempt.attemptCount());
        OutboxEventJpaEntity afterStaleSuccess = repository.required(eventId);
        assertEquals(OutboxEventStatus.PROCESSING, afterStaleSuccess.getStatus());
        assertEquals(2, afterStaleSuccess.getAttemptCount());

        store.markFailed(eventId, firstAttempt.attemptCount(), "late failure");
        OutboxEventJpaEntity afterStaleFailure = repository.required(eventId);
        assertEquals(OutboxEventStatus.PROCESSING, afterStaleFailure.getStatus());
        assertEquals(2, afterStaleFailure.getAttemptCount());
        assertNull(afterStaleFailure.getLastError());

        store.markProcessed(eventId, secondAttempt.attemptCount());
        OutboxEventJpaEntity processed = repository.required(eventId);
        assertEquals(OutboxEventStatus.PROCESSED, processed.getStatus());
        assertEquals(2, processed.getAttemptCount());
    }

    private static UUID append(OutboxEventStore store) {
        return store.append(
                "APPLICATION",
                UUID.randomUUID().toString(),
                "APPLICATION_PROVISIONING_REQUESTED",
                null
        );
    }

    private static OutboxWorkerProperties properties(int maxAttempts) {
        OutboxWorkerProperties properties = new OutboxWorkerProperties();
        properties.setBatchSize(20);
        properties.setMaxAttempts(maxAttempts);
        properties.setInitialBackoff(Duration.ofSeconds(2));
        properties.setMaxBackoff(Duration.ofSeconds(30));
        properties.setProcessingTimeout(Duration.ofMinutes(5));
        return properties;
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    private static final class InMemoryOutboxRepository {
        private final Map<UUID, OutboxEventJpaEntity> entities = new LinkedHashMap<>();

        OutboxEventJpaRepository proxy() {
            return (OutboxEventJpaRepository) Proxy.newProxyInstance(
                    OutboxEventJpaRepository.class.getClassLoader(),
                    new Class<?>[]{OutboxEventJpaRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "save" -> save((OutboxEventJpaEntity) args[0]);
                        case "saveAll" -> saveAll((Iterable<?>) args[0]);
                        case "findById", "lockById" -> Optional.ofNullable(entities.get((UUID) args[0]));
                        case "lockClaimableEvents" -> claimable(
                                (Instant) args[0],
                                (Instant) args[1],
                                (Integer) args[2]
                        );
                        case "toString" -> "InMemoryOutboxRepository";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> throw new UnsupportedOperationException(
                                "Unexpected repository method in test: " + method
                        );
                    }
            );
        }

        OutboxEventJpaEntity required(UUID id) {
            OutboxEventJpaEntity entity = entities.get(id);
            if (entity == null) {
                throw new IllegalStateException("Missing test event: " + id);
            }
            return entity;
        }

        private OutboxEventJpaEntity save(OutboxEventJpaEntity entity) {
            entities.put(entity.getId(), entity);
            return entity;
        }

        private List<OutboxEventJpaEntity> saveAll(Iterable<?> values) {
            List<OutboxEventJpaEntity> saved = new ArrayList<>();
            for (Object value : values) {
                saved.add(save((OutboxEventJpaEntity) value));
            }
            return saved;
        }

        private List<OutboxEventJpaEntity> claimable(
                Instant now,
                Instant staleBefore,
                int batchSize
        ) {
            return entities.values().stream()
                    .filter(entity -> isClaimable(entity, now, staleBefore))
                    .sorted(Comparator.comparing(OutboxEventJpaEntity::getAvailableAt))
                    .limit(batchSize)
                    .toList();
        }

        private static boolean isClaimable(
                OutboxEventJpaEntity entity,
                Instant now,
                Instant staleBefore
        ) {
            if (entity.getStatus() == OutboxEventStatus.PENDING) {
                return !entity.getAvailableAt().isAfter(now);
            }

            return entity.getStatus() == OutboxEventStatus.PROCESSING
                    && entity.getProcessingStartedAt() != null
                    && !entity.getProcessingStartedAt().isAfter(staleBefore);
        }
    }
}