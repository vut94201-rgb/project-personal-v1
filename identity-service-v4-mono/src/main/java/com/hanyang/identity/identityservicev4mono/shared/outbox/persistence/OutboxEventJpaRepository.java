package com.hanyang.identity.identityservicev4mono.shared.outbox.persistence;

import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxEventJpaRepository
        extends JpaRepository<OutboxEventJpaEntity, UUID> {

    long countByStatus(OutboxEventStatus status);

    List<OutboxEventJpaEntity> findByStatusOrderByUpdatedAtDesc(
            OutboxEventStatus status,
            Pageable pageable
    );

    Optional<OutboxEventJpaEntity> findFirstByStatusOrderByCreatedAtAsc(
            OutboxEventStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event
            from OutboxEventJpaEntity event
            where event.id = :eventId
            """)
    Optional<OutboxEventJpaEntity> lockById(
            @Param("eventId") UUID eventId
    );

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