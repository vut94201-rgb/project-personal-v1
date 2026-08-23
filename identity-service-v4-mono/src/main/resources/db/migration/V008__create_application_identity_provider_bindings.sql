CREATE TABLE application_identity_provider_bindings
(
    id                UUID          NOT NULL,
    application_id    UUID          NOT NULL,
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

    CONSTRAINT pk_application_idp_bindings
        PRIMARY KEY (id),

    CONSTRAINT fk_application_idp_bindings_application
        FOREIGN KEY (application_id)
        REFERENCES applications(id),

    CONSTRAINT uk_application_idp_bindings_application_provider
        UNIQUE (application_id, provider),

    CONSTRAINT ck_application_idp_bindings_sync_status
        CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED', 'DRIFTED')),

    CONSTRAINT ck_application_idp_bindings_revisions
        CHECK (
            desired_revision >= 1
            AND synced_revision >= 0
            AND synced_revision <= desired_revision
        )
);

CREATE INDEX idx_application_idp_bindings_sync_status
    ON application_identity_provider_bindings (provider, sync_status);

INSERT INTO application_identity_provider_bindings
(
    id,
    application_id,
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
    application.id,
    'KEYCLOAK',
    'PENDING',
    1,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM applications application;
