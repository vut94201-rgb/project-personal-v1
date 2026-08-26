CREATE TABLE employee_profiles (
    employee_id   UUID         NOT NULL,
    email         VARCHAR(254),
    phone_number  VARCHAR(50),
    address       VARCHAR(500),
    hire_date     DATE,

    version       BIGINT       NOT NULL DEFAULT 0,

    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),

    CONSTRAINT pk_employee_profiles
        PRIMARY KEY (employee_id),

    CONSTRAINT fk_employee_profiles_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
);

CREATE UNIQUE INDEX uk_employee_profiles_email_ci
    ON employee_profiles (LOWER(email))
    WHERE email IS NOT NULL;
