CREATE TABLE role_permissions
(
    role_id        UUID         NOT NULL,
    permission_id  UUID         NOT NULL,

    version        BIGINT       NOT NULL DEFAULT 0,

    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),

    CONSTRAINT pk_role_permissions
        PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id),

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions (id)
);

CREATE INDEX idx_role_permissions_permission_id
    ON role_permissions (permission_id);
