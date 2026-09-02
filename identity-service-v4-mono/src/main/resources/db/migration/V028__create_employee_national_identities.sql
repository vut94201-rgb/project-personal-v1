CREATE TABLE employee_national_identities (
    id                   UUID         NOT NULL,
    employee_id          UUID         NOT NULL,
    country_code         VARCHAR(2)   NOT NULL,
    identity_type        VARCHAR(32)  NOT NULL,
    encrypted_number     TEXT         NOT NULL,
    number_fingerprint   VARCHAR(64)  NOT NULL,
    last_four            VARCHAR(4)   NOT NULL,

    version              BIGINT       NOT NULL DEFAULT 0,

    created_at           TIMESTAMPTZ  NOT NULL,
    updated_at           TIMESTAMPTZ  NOT NULL,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),

    CONSTRAINT pk_employee_national_identities
        PRIMARY KEY (id),

    CONSTRAINT fk_employee_national_identities_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id),

    CONSTRAINT uk_employee_national_identities_employee_type
        UNIQUE (employee_id, country_code, identity_type),

    CONSTRAINT uk_employee_national_identities_fingerprint
        UNIQUE (country_code, identity_type, number_fingerprint)
);

CREATE INDEX idx_employee_national_identities_employee
    ON employee_national_identities (employee_id);
