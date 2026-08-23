CREATE TABLE permissions
(
    id              UUID          NOT NULL,
    application_id  UUID          NOT NULL,
    code            VARCHAR(100)  NOT NULL,
    name            VARCHAR(150)  NOT NULL,
    status          VARCHAR(3)    NOT NULL,

    version         BIGINT        NOT NULL DEFAULT 0,

    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),

    CONSTRAINT pk_permissions
        PRIMARY KEY (id),

    CONSTRAINT fk_permissions_application
        FOREIGN KEY (application_id)
        REFERENCES applications (id),

    CONSTRAINT uk_permissions_application_code
        UNIQUE (application_id, code),

    CONSTRAINT ck_permissions_status
        CHECK (status IN ('ACT', 'DIS'))
);
