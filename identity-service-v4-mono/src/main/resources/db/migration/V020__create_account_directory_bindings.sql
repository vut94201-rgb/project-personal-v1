CREATE TABLE account_directory_bindings
(
    id                UUID          NOT NULL,
    account_id        UUID          NOT NULL,
    provider          VARCHAR(30)   NOT NULL,

    external_dn       VARCHAR(500),
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

    CONSTRAINT pk_account_directory_bindings
        PRIMARY KEY (id),

    CONSTRAINT fk_account_directory_bindings_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT uk_account_directory_bindings_account_provider
        UNIQUE (account_id, provider),

    CONSTRAINT ck_account_directory_bindings_provider
        CHECK (provider IN ('DS389')),

    CONSTRAINT ck_account_directory_bindings_sync_status
        CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED', 'DRIFTED')),

    CONSTRAINT ck_account_directory_bindings_revisions
        CHECK (
            desired_revision >= 1
            AND synced_revision >= 0
            AND synced_revision <= desired_revision
        )
);

CREATE INDEX idx_account_directory_bindings_sync_status
    ON account_directory_bindings (provider, sync_status);
