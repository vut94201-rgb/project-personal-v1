package com.hanyang.identity.identityservicev4mono.shared.outbox;

public interface OutboxEventHandler {

            boolean supports(String eventType);

            void handle(OutboxEvent event);
}