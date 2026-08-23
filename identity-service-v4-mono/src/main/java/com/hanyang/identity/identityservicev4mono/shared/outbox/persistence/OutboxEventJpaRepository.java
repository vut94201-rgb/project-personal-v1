package com.hanyang.identity.identityservicev4mono.shared.outbox.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository
        extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query(
            value = """
                    SELECT *
                    FROM outbox_events
                    WHERE (
                            status = 'PENDING'
                            AND available_at <= :now
                        )
                        OR (
                            status = 'PROCESSING'
                            AND processing_started_at <= :staleBefore
                        )
                    ORDER BY available_at, created_at
                    LIMIT :batchSize
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<OutboxEventJpaEntity> lockClaimableEvents(
            @Param("now") Instant now,
            @Param("staleBefore") Instant staleBefore,
            @Param("batchSize") int batchSize
    );
}