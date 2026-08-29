CREATE TABLE service_principal_owners
(
    id                    UUID         NOT NULL,
    service_principal_id  UUID         NOT NULL,
    employee_id           UUID         NOT NULL,
    ownership_type        VARCHAR(3)   NOT NULL,
    status                VARCHAR(3)   NOT NULL,
    revoked_at            TIMESTAMPTZ,

    version               BIGINT       NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),

    CONSTRAINT pk_service_principal_owners
        PRIMARY KEY (id),

    CONSTRAINT fk_sp_owners_service_principal
        FOREIGN KEY (service_principal_id)
        REFERENCES service_principals (id),

    CONSTRAINT fk_sp_owners_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees (id),

    CONSTRAINT ck_sp_owners_ownership_type
        CHECK (ownership_type IN ('PRI', 'TEC')),

    CONSTRAINT ck_sp_owners_status
        CHECK (status IN ('ACT', 'REV')),

    CONSTRAINT ck_sp_owners_lifecycle
        CHECK (
            (status = 'ACT' AND revoked_at IS NULL)
            OR
            (status = 'REV' AND revoked_at IS NOT NULL)
        )
);

-- Preserve ownership history while preventing duplicate live assignments.
CREATE UNIQUE INDEX uk_sp_owners_active_employee
    ON service_principal_owners (service_principal_id, employee_id)
    WHERE status = 'ACT';

-- A service principal may have zero or one active PRIMARY owner and any
-- number of active TECHNICAL owners. Requiring a PRIMARY owner before
-- activation belongs to the later lifecycle coordinator/application policy.
CREATE UNIQUE INDEX uk_sp_owners_active_primary
    ON service_principal_owners (service_principal_id)
    WHERE status = 'ACT' AND ownership_type = 'PRI';

CREATE INDEX idx_sp_owners_service_principal_status
    ON service_principal_owners (service_principal_id, status);

CREATE INDEX idx_sp_owners_employee_status
    ON service_principal_owners (employee_id, status);
