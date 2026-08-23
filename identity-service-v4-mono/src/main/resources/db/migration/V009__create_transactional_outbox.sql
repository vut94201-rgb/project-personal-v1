CREATE TABLE outbox_events
(
    id                    UUID          NOT NULL,
    aggregate_type        VARCHAR(80)   NOT NULL,
    aggregate_id          VARCHAR(200)  NOT NULL,
    event_type            VARCHAR(120)  NOT NULL,
    payload               TEXT,

    status                VARCHAR(20)   NOT NULL,
    attempt_count         INTEGER       NOT NULL DEFAULT 0,
    available_at          TIMESTAMPTZ   NOT NULL,
    processing_started_at TIMESTAMPTZ,
    processed_at          TIMESTAMPTZ,
    last_error            VARCHAR(2000),

    version               BIGINT        NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ   NOT NULL,
    updated_at            TIMESTAMPTZ   NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),

    CONSTRAINT pk_outbox_events
        PRIMARY KEY (id),

    CONSTRAINT ck_outbox_events_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'DEAD')),

    CONSTRAINT ck_outbox_events_attempt_count
        CHECK (attempt_count >= 0)
);

CREATE INDEX idx_outbox_events_pending_delivery
    ON outbox_events (available_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_events_processing_timeout
    ON outbox_events (processing_started_at)
    WHERE status = 'PROCESSING';

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (aggregate_type, aggregate_id, event_type);

INSERT INTO outbox_events
(
    id,
    aggregate_type,
    aggregate_id,
    event_type,
    payload,
    status,
    attempt_count,
    available_at,
    version,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    'APPLICATION',
    binding.application_id::TEXT,
    'APPLICATION_PROVISIONING_REQUESTED',
    NULL,
    'PENDING',
    0,
    CURRENT_TIMESTAMP,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM application_identity_provider_bindings binding
WHERE binding.provider = 'KEYCLOAK'
  AND binding.sync_status <> 'SYNCED';
