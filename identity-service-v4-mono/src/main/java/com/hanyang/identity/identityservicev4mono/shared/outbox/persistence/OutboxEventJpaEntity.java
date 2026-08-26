package com.hanyang.identity.identityservicev4mono.shared.outbox.persistence;

import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEventJpaEntity extends AuditableEntity {

  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "aggregate_type", nullable = false, length = 80)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false, length = 200)
  private String aggregateId;

  @Column(name = "event_type", nullable = false, length = 120)
  private String eventType;

  @Column(name = "payload", columnDefinition = "TEXT")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private OutboxEventStatus status;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "available_at", nullable = false)
  private Instant availableAt;

  @Column(name = "processing_started_at")
  private Instant processingStartedAt;

  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "last_error", length = 2000)
  private String lastError;
}
