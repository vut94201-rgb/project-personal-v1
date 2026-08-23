package com.hanyang.identity.identityservicev4mono.shared.outbox;


import com.hanyang.identity.identityservicev4mono.shared.outbox.persistence.OutboxEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "outbox.worker",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class OutboxWorker {

    private final OutboxEventStore eventStore;
    private final
    List<OutboxEventHandler> handlers;

    @Scheduled(fixedDelayString = "${outbox.worker.fixed-delay-ms:1000}")
    public void processAvailableEvents() {
        List<OutboxEvent> events = eventStore.claimBatch();

        for (OutboxEvent event : events) {
            try {
                handlerFor(event).handle(event);
                eventStore.markProcessed(event.id());
            } catch (RuntimeException exception) {
                String error = messageOf(exception);
                eventStore.markFailed(event.id(), error);

                log.warn(
                        "Outbox event delivery failed. eventId={}, eventType={}, aggregateType={}, aggregateId={}, attempt={}",
                        event.id(),
                        event.eventType(),
                        event.aggregateType(),
                        event.aggregateId(),
                        event.attemptCount(),
                        exception
                );
            }
        }
    }

    private OutboxEventHandler handlerFor(OutboxEvent event) {
        return handlers.stream()
                .filter(handler -> handler.supports(event.eventType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No outbox handler registered for event type: " + event.eventType()
                ));
    }

    private static String messageOf(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}