CREATE TABLE applications
(
    id          UUID         NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(150) NOT NULL,
    status      VARCHAR(3)   NOT NULL,

    version     BIGINT       NOT NULL DEFAULT 0,

    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),

    CONSTRAINT pk_applications
        PRIMARY KEY (id),

    CONSTRAINT uk_applications_code
        UNIQUE (code),

    CONSTRAINT ck_applications_status
        CHECK (status IN ('ACT', 'DIS'))
);
