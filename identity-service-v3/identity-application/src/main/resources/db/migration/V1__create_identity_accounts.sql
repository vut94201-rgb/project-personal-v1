CREATE  SEQUENCE  identity_user_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE identity_users
(
    id               BIGINT                      NOT NULL,
    deleted          BOOLEAN                     NOT NULL,
    deleted_at       TIMESTAMP WITHOUT TIME ZONE,
    deleted_by       VARCHAR(100),
    version          BIGINT                      NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    keycloak_user_id UUID                        NOT NULL,
    username         VARCHAR(100)                NOT NULL,
    status           VARCHAR(1)                  NOT NULL,
    email            VARCHAR(320)                NOT NULL,
    phone_number     VARCHAR(80),
    gender           VARCHAR(1)                  NOT NULL,
    CONSTRAINT pk_identity_users PRIMARY KEY (id)
);

ALTER TABLE identity_users
    ADD CONSTRAINT uc_identity_users_email UNIQUE (email);

ALTER TABLE identity_users
    ADD CONSTRAINT uc_identity_users_keycloak_user UNIQUE (keycloak_user_id);

ALTER TABLE identity_users
    ADD CONSTRAINT uc_identity_users_username UNIQUE (username);