CREATE TABLE employees (
    id            UUID         NOT NULL,
    employee_code VARCHAR(50)  NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    status        VARCHAR(3)   NOT NULL,

    version       BIGINT       NOT NULL DEFAULT 0,

    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),

    CONSTRAINT pk_employees
        PRIMARY KEY (id),

    CONSTRAINT uk_employees_employee_code
        UNIQUE (employee_code),

    CONSTRAINT ck_employees_status
        CHECK (status IN ('ACT', 'INA', 'TER'))
);
