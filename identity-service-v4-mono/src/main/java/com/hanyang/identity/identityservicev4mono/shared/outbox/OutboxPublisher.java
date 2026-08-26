package com.hanyang.identity.identityservicev4mono.shared.outbox;

import com.hanyang.identity.identityservicev4mono.shared.outbox.persistence.OutboxEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

  private final OutboxEventStore eventStore;

  @Transactional(propagation = Propagation.MANDATORY)
  public UUID publish(String aggregateType, String aggregateId, String eventType, String payload) {
    return eventStore.append(aggregateType, aggregateId, eventType, payload);
  }
}
