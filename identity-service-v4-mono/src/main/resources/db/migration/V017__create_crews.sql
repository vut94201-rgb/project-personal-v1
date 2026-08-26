CREATE TABLE crews
(
    id              UUID         NOT NULL,
    department_id   UUID         NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    status          VARCHAR(3)   NOT NULL,

    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),

    CONSTRAINT pk_crews
        PRIMARY KEY (id),

    CONSTRAINT fk_crews_department
        FOREIGN KEY (department_id)
        REFERENCES departments (id),

    CONSTRAINT uk_crews_department_code
        UNIQUE (department_id, code),

    CONSTRAINT ck_crews_status
        CHECK (status IN ('ACT', 'DIS'))
);

CREATE INDEX idx_crews_department_id
    ON crews (department_id);
