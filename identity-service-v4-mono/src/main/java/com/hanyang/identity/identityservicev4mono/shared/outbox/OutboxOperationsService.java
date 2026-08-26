package com.hanyang.identity.identityservicev4mono.shared.outbox;

import com.hanyang.identity.identityservicev4mono.shared.outbox.persistence.OutboxEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxOperationsService {

    private final OutboxEventStore eventStore;
    private final OutboxOperationalMetrics metrics;

    public List<OutboxEventSnapshot> findDeadEvents(int limit) {
        return eventStore.findDeadEvents(limit);
    }

    public OutboxEventSnapshot retryDeadEvent(UUID eventId) {
        OutboxEventSnapshot retried = eventStore.retryDead(eventId);
        metrics.recordManualRetry();
        return retried;
    }
}