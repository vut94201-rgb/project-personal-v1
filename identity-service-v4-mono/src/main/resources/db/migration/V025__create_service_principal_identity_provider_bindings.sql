CREATE TABLE service_principal_identity_provider_bindings
(
    id                    UUID          NOT NULL,
    service_principal_id  UUID          NOT NULL,
    provider              VARCHAR(30)   NOT NULL,

    external_id           VARCHAR(100),
    external_code         VARCHAR(100),

    sync_status           VARCHAR(20)   NOT NULL,
    desired_revision      BIGINT        NOT NULL,
    synced_revision       BIGINT        NOT NULL,

    last_synced_at        TIMESTAMPTZ,
    last_error            VARCHAR(2000),

    version               BIGINT        NOT NULL DEFAULT 0,

    created_at            TIMESTAMPTZ   NOT NULL,
    updated_at            TIMESTAMPTZ   NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),

    CONSTRAINT pk_service_principal_idp_bindings
        PRIMARY KEY (id),

    CONSTRAINT fk_service_principal_idp_bindings_principal
        FOREIGN KEY (service_principal_id)
        REFERENCES service_principals(id),

    CONSTRAINT uk_service_principal_idp_bindings_principal_provider
        UNIQUE (service_principal_id, provider),

    CONSTRAINT ck_service_principal_idp_bindings_provider
        CHECK (provider IN ('KEYCLOAK')),

    CONSTRAINT ck_service_principal_idp_bindings_sync_status
        CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED', 'DRIFTED')),

    CONSTRAINT ck_service_principal_idp_bindings_revisions
        CHECK (
            desired_revision >= 1
            AND synced_revision >= 0
            AND synced_revision <= desired_revision
        )
);

CREATE INDEX idx_service_principal_idp_bindings_sync_status
    ON service_principal_identity_provider_bindings (provider, sync_status);

CREATE UNIQUE INDEX uk_service_principal_idp_bindings_provider_external_id
    ON service_principal_identity_provider_bindings (provider, external_id)
    WHERE external_id IS NOT NULL;

INSERT INTO service_principal_identity_provider_bindings
(
    id,
    service_principal_id,
    provider,
    sync_status,
    desired_revision,
    synced_revision,
    version,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    service_principal.id,
    'KEYCLOAK',
    'PENDING',
    1,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM service_principals service_principal;

-- Intentionally no outbox backfill in this migration.
-- The provider adapter/handler is introduced in the next commit; publishing
-- work here would allow the current worker to consume an event with no handler
-- and incorrectly exhaust it into DEAD before reconciliation exists.
