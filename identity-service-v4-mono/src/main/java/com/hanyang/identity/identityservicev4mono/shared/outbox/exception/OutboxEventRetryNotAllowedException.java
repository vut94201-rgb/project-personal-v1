package com.hanyang.identity.identityservicev4mono.shared.outbox.exception;


import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventStatus;

import java.util.UUID;

public class OutboxEventRetryNotAllowedException extends RuntimeException {

    public OutboxEventRetryNotAllowedException(
            UUID eventId,
            OutboxEventStatus status
    ) {
            super(
                                "Only DEAD outbox events can be retried manually. eventId="
                                                + eventId
                                                + ", status="
                                               + status
                                );
            }
}