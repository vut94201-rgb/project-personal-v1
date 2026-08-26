package com.hanyang.identity.identityservicev4mono.shared.outbox.exception;

import java.util.UUID;

public class OutboxEventNotFoundException extends RuntimeException {
    public OutboxEventNotFoundException(UUID eventId) {
              super("Outbox event not found: " + eventId);
            }
}