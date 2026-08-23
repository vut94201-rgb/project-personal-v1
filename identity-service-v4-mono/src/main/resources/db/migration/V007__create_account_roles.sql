CREATE TABLE account_roles
(
    account_id  UUID         NOT NULL,
    role_id     UUID         NOT NULL,

    version     BIGINT       NOT NULL DEFAULT 0,

    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),

    CONSTRAINT pk_account_roles
        PRIMARY KEY (account_id, role_id),

    CONSTRAINT fk_account_roles_account
        FOREIGN KEY (account_id)
        REFERENCES accounts (id),

    CONSTRAINT fk_account_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id)
);

CREATE INDEX idx_account_roles_role_id
    ON account_roles (role_id);
