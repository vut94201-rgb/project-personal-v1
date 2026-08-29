CREATE TABLE service_principal_role_identity_provider_bindings
(
    id                    UUID          NOT NULL,
    service_principal_id  UUID          NOT NULL,
    role_id               UUID          NOT NULL,
    provider              VARCHAR(30)   NOT NULL,

    desired_assigned      BOOLEAN       NOT NULL,
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

    CONSTRAINT pk_service_principal_role_idp_bindings
        PRIMARY KEY (id),

    CONSTRAINT fk_service_principal_role_idp_bindings_principal
        FOREIGN KEY (service_principal_id)
        REFERENCES service_principals(id),

    CONSTRAINT fk_service_principal_role_idp_bindings_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id),

    CONSTRAINT uk_service_principal_role_idp_bindings_key_provider
        UNIQUE (service_principal_id, role_id, provider),

    CONSTRAINT ck_service_principal_role_idp_bindings_provider
        CHECK (provider IN ('KEYCLOAK')),

    CONSTRAINT ck_service_principal_role_idp_bindings_sync_status
        CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED', 'DRIFTED')),

    CONSTRAINT ck_service_principal_role_idp_bindings_revisions
        CHECK (
            desired_revision >= 1
            AND synced_revision >= 0
            AND synced_revision <= desired_revision
        )
);

CREATE INDEX idx_service_principal_role_idp_bindings_sync_status
    ON service_principal_role_identity_provider_bindings (provider, sync_status);

CREATE INDEX idx_service_principal_role_idp_bindings_principal
    ON service_principal_role_identity_provider_bindings (service_principal_id);

CREATE INDEX idx_service_principal_role_idp_bindings_role
    ON service_principal_role_identity_provider_bindings (role_id);

INSERT INTO service_principal_role_identity_provider_bindings
(
    id,
    service_principal_id,
    role_id,
    provider,
    desired_assigned,
    sync_status,
    desired_revision,
    synced_revision,
    version,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    assignment.service_principal_id,
    assignment.role_id,
    'KEYCLOAK',
    TRUE,
    'PENDING',
    1,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM service_principal_roles assignment;


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
    'SERVICE_PRINCIPAL_ROLE',
    binding.service_principal_id::TEXT || ':' || binding.role_id::TEXT,
    'SERVICE_PRINCIPAL_ROLE_PROVISIONING_REQUESTED',
    NULL,
    'PENDING',
    0,
    CURRENT_TIMESTAMP,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM service_principal_role_identity_provider_bindings binding
JOIN service_principal_identity_provider_bindings principal_binding
  ON principal_binding.service_principal_id = binding.service_principal_id
 AND principal_binding.provider = 'KEYCLOAK'
WHERE binding.provider = 'KEYCLOAK'
  AND binding.sync_status <> 'SYNCED'
  AND principal_binding.external_id IS NOT NULL
  AND NOT EXISTS
  (
      SELECT 1
      FROM outbox_events existing
      WHERE existing.aggregate_type = 'SERVICE_PRINCIPAL_ROLE'
        AND existing.aggregate_id =
            binding.service_principal_id::TEXT || ':' || binding.role_id::TEXT
        AND existing.event_type =
            'SERVICE_PRINCIPAL_ROLE_PROVISIONING_REQUESTED'
        AND existing.status IN ('PENDING', 'PROCESSING')
  );
