CREATE TABLE service_principal_roles
(
    service_principal_id  UUID         NOT NULL,
    role_id               UUID         NOT NULL,

    version               BIGINT       NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),

    CONSTRAINT pk_service_principal_roles
        PRIMARY KEY (service_principal_id, role_id),

    CONSTRAINT fk_sp_roles_service_principal
        FOREIGN KEY (service_principal_id)
        REFERENCES service_principals (id),

    CONSTRAINT fk_sp_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id)
);

CREATE INDEX idx_sp_roles_role_id
    ON service_principal_roles (role_id);
