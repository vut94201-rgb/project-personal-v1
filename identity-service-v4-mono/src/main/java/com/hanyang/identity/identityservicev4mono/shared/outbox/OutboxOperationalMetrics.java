package com.hanyang.identity.identityservicev4mono.shared.outbox;

import com.hanyang.identity.identityservicev4mono.shared.outbox.persistence.OutboxEventJpaRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class OutboxOperationalMetrics {

    private final OutboxEventJpaRepository repository;
    private final Clock clock;

    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Counter manualRetryCounter;

    public OutboxOperationalMetrics(
            OutboxEventJpaRepository repository,
            MeterRegistry meterRegistry,
            Clock clock
    ) {
        this.repository = repository;
        this.clock = clock;

        registerStatusGauge(meterRegistry, OutboxEventStatus.PENDING);
        registerStatusGauge(meterRegistry, OutboxEventStatus.PROCESSING);
        registerStatusGauge(meterRegistry, OutboxEventStatus.DEAD);

        Gauge.builder(
                        "identity.outbox.oldest.pending.age.seconds",
                        this,
                        OutboxOperationalMetrics::oldestPendingAgeSeconds
                )
                .description("Age in seconds of the oldest PENDING outbox event")
                .register(meterRegistry);

        this.processedCounter = Counter.builder("identity.outbox.delivery")
                .description("Outbox delivery outcomes")
                .tag("outcome", "processed")
                .register(meterRegistry);

        this.failedCounter = Counter.builder("identity.outbox.delivery")
                .description("Outbox delivery outcomes")
                .tag("outcome", "failed")
                .register(meterRegistry);

        this.manualRetryCounter = Counter.builder("identity.outbox.manual.retry")
                .description("Number of DEAD outbox events manually returned to PENDING")
                .register(meterRegistry);
    }

    public void recordProcessed() {
        processedCounter.increment();
    }

    public void recordFailed() {
        failedCounter.increment();
    }

    public void recordManualRetry() {
        manualRetryCounter.increment();
    }

    private void registerStatusGauge(
            MeterRegistry meterRegistry,
            OutboxEventStatus status
    ) {
        Gauge.builder(
                        "identity.outbox.events",
                        repository,
                        value -> value.countByStatus(status)
                )
                .description("Current outbox event count by status")
                .tag("status", status.name())
                .register(meterRegistry);
    }

    private double oldestPendingAgeSeconds() {
        return repository.findFirstByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING)
                .map(event -> ageSeconds(event.getCreatedAt()))
                .orElse(0.0d);
    }

    private double ageSeconds(Instant createdAt) {
        Duration age = Duration.between(createdAt, clock.instant());
        return Math.max(0L, age.toSeconds());
    }
}