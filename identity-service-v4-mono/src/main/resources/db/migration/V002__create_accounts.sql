CREATE TABLE accounts
(
    id                UUID         NOT NULL,
    employee_id       UUID         NOT NULL,
    username          VARCHAR(100) NOT NULL,
    keycloak_subject  VARCHAR(100),
    status            VARCHAR(3)   NOT NULL,

    version            BIGINT       NOT NULL DEFAULT 0,

    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),

    CONSTRAINT pk_accounts
        PRIMARY KEY (id),

    CONSTRAINT uk_accounts_employee
        UNIQUE (employee_id),

    CONSTRAINT uk_accounts_username
        UNIQUE (username),

    CONSTRAINT uk_accounts_keycloak_subject
        UNIQUE (keycloak_subject),

    CONSTRAINT fk_accounts_employee
        FOREIGN KEY (employee_id)
            REFERENCES employees(id),

    CONSTRAINT ck_accounts_status
        CHECK (status IN ('PND', 'ACT', 'DIS'))
);