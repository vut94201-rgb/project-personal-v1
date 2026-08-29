CREATE TABLE service_principals
(
    id            UUID          NOT NULL,
    code          VARCHAR(100)  NOT NULL,
    display_name  VARCHAR(150)  NOT NULL,
    purpose       VARCHAR(500)  NOT NULL,
    description   VARCHAR(1000),
    status        VARCHAR(3)    NOT NULL,

    version       BIGINT        NOT NULL DEFAULT 0,

    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),

    CONSTRAINT pk_service_principals
        PRIMARY KEY (id),

    CONSTRAINT uk_service_principals_code
        UNIQUE (code),

    CONSTRAINT ck_service_principals_status
        CHECK (status IN ('PND', 'ACT', 'DIS'))
);

CREATE INDEX idx_service_principals_status
    ON service_principals (status);
