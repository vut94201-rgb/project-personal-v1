CREATE TABLE organizational_assignments
(
    id              UUID         NOT NULL,
    employee_id     UUID         NOT NULL,
    department_id   UUID         NOT NULL,
    position_id     UUID         NOT NULL,
    effective_from  DATE         NOT NULL,
    effective_to    DATE,
    status          VARCHAR(3)   NOT NULL,

    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),

    CONSTRAINT pk_organizational_assignments
        PRIMARY KEY (id),

    CONSTRAINT fk_org_assignments_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees (id),

    CONSTRAINT fk_org_assignments_department
        FOREIGN KEY (department_id)
        REFERENCES departments (id),

    CONSTRAINT fk_org_assignments_position
        FOREIGN KEY (position_id)
        REFERENCES positions (id),

    CONSTRAINT ck_org_assignments_status
        CHECK (status IN ('ACT', 'END')),

    CONSTRAINT ck_org_assignments_dates
        CHECK (effective_to IS NULL OR effective_to >= effective_from),

    CONSTRAINT ck_org_assignments_lifecycle
        CHECK (
            (status = 'ACT' AND effective_to IS NULL)
            OR
            (status = 'END' AND effective_to IS NOT NULL)
        )
);

CREATE INDEX idx_org_assignments_employee_id
    ON organizational_assignments (employee_id);

CREATE INDEX idx_org_assignments_department_id
    ON organizational_assignments (department_id);

CREATE INDEX idx_org_assignments_position_id
    ON organizational_assignments (position_id);

CREATE UNIQUE INDEX uk_org_assignments_employee_active
    ON organizational_assignments (employee_id)
    WHERE status = 'ACT';
