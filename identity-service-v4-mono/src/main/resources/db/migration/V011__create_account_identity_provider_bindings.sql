CREATE TABLE account_identity_provider_bindings
(
    id                UUID          NOT NULL,
    account_id        UUID          NOT NULL,
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

    CONSTRAINT pk_account_idp_bindings
        PRIMARY KEY (id),

    CONSTRAINT fk_account_idp_bindings_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT uk_account_idp_bindings_account_provider
        UNIQUE (account_id, provider),

    CONSTRAINT ck_account_idp_bindings_sync_status
        CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED', 'DRIFTED')),

    CONSTRAINT ck_account_idp_bindings_revisions
        CHECK (
            desired_revision >= 1
            AND synced_revision >= 0
            AND synced_revision <= desired_revision
        )
);

CREATE INDEX idx_account_idp_bindings_sync_status
    ON account_identity_provider_bindings (provider, sync_status);

INSERT INTO account_identity_provider_bindings
(
    id,
    account_id,
    provider,
    external_id,
    external_code,
    sync_status,
    desired_revision,
    synced_revision,
    version,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    account.id,
    'KEYCLOAK',
    account.keycloak_subject,
    account.username,
    'PENDING',
    1,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM accounts account;

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
    'ACCOUNT',
    binding.account_id::TEXT,
    'ACCOUNT_PROVISIONING_REQUESTED',
    NULL,
    'PENDING',
    0,
    CURRENT_TIMESTAMP,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account_identity_provider_bindings binding
WHERE binding.provider = 'KEYCLOAK'
  AND binding.sync_status <> 'SYNCED';
