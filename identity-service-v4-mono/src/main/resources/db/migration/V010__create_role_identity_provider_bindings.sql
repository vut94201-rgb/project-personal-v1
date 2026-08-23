CREATE TABLE role_identity_provider_bindings
(
    id                UUID          NOT NULL,
    role_id           UUID          NOT NULL,
    provider          VARCHAR(30)   NOT NULL,

    external_id       VARCHAR(100),
    external_code     VARCHAR(100),

    sync_status       VARCHAR(20)   NOT NULL,
    desired_revision  BIGINT        NOT NULL,
    synced_revision   BIGINT        NOT NULL,

    last_synced_at    TIMESTAMPTZ,
    last_error        VARCHAR(2000),

    version           BIGINT        NOT NULL DEFAULT 0,

    created_at        TIMESTAMPTZ   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),

    CONSTRAINT pk_role_idp_bindings
        PRIMARY KEY (id),

    CONSTRAINT fk_role_idp_bindings_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id),

    CONSTRAINT uk_role_idp_bindings_role_provider
        UNIQUE (role_id, provider),

    CONSTRAINT ck_role_idp_bindings_sync_status
        CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED', 'DRIFTED')),

    CONSTRAINT ck_role_idp_bindings_revisions
        CHECK (
            desired_revision >= 1
            AND synced_revision >= 0
            AND synced_revision <= desired_revision
        )
);

CREATE INDEX idx_role_idp_bindings_sync_status
    ON role_identity_provider_bindings (provider, sync_status);

INSERT INTO role_identity_provider_bindings
(
    id,
    role_id,
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
    role.id,
    'KEYCLOAK',
    'PENDING',
    1,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM roles role;

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
    'ROLE',
    binding.role_id::TEXT,
    'ROLE_PROVISIONING_REQUESTED',
    NULL,
    'PENDING',
    0,
    CURRENT_TIMESTAMP,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM role_identity_provider_bindings binding
WHERE binding.provider = 'KEYCLOAK'
  AND binding.sync_status <> 'SYNCED';
