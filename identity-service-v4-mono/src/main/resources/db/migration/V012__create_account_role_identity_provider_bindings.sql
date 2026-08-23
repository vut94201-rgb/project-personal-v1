CREATE TABLE account_role_identity_provider_bindings
(
    id                UUID          NOT NULL,
    account_id        UUID          NOT NULL,
    role_id           UUID          NOT NULL,
    provider          VARCHAR(30)   NOT NULL,

    desired_assigned  BOOLEAN       NOT NULL,
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

    CONSTRAINT pk_account_role_idp_bindings
        PRIMARY KEY (id),

    CONSTRAINT fk_account_role_idp_bindings_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_account_role_idp_bindings_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id),

    CONSTRAINT uk_account_role_idp_bindings_key_provider
        UNIQUE (account_id, role_id, provider),

    CONSTRAINT ck_account_role_idp_bindings_sync_status
        CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED', 'DRIFTED')),

    CONSTRAINT ck_account_role_idp_bindings_revisions
        CHECK (
            desired_revision >= 1
            AND synced_revision >= 0
            AND synced_revision <= desired_revision
        )
);

CREATE INDEX idx_account_role_idp_bindings_sync_status
    ON account_role_identity_provider_bindings (provider, sync_status);

CREATE INDEX idx_account_role_idp_bindings_account
    ON account_role_identity_provider_bindings (account_id);

CREATE INDEX idx_account_role_idp_bindings_role
    ON account_role_identity_provider_bindings (role_id);

INSERT INTO account_role_identity_provider_bindings
(
    id,
    account_id,
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
    account_role.account_id,
    account_role.role_id,
    'KEYCLOAK',
    TRUE,
    'PENDING',
    1,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account_roles account_role;

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
    'ACCOUNT_ROLE',
    binding.account_id::TEXT || ':' || binding.role_id::TEXT,
    'ACCOUNT_ROLE_PROVISIONING_REQUESTED',
    NULL,
    'PENDING',
    0,
    CURRENT_TIMESTAMP,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account_role_identity_provider_bindings binding
WHERE binding.provider = 'KEYCLOAK'
  AND binding.sync_status <> 'SYNCED';
