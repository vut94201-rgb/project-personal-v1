package com.hanyang.identity.identityservicev4mono.shared.outbox;

public enum OutboxEventStatus {
  PENDING,
  PROCESSING,
  PROCESSED,
  DEAD
}
