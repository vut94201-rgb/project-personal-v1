package com.hanyang.identity.identityservicev4mono.employee.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Employee {
    private final EmployeeId id;
    private final String employeeCode;

    private String fullName;
    private EmployeeStatus status;

    public Employee(
            EmployeeId id,
            String employeeCode,
            String fullName,
            EmployeeStatus status
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.employeeCode = requireText(employeeCode, "employeeCode");
        this.fullName = requireText(fullName, "fullName");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public static Employee create(
            EmployeeId id,
            String employeeCode,
            String fullName
    ) {
        return new Employee(
                id,
                employeeCode,
                fullName,
                EmployeeStatus.ACTIVE
        );
    }


    public static Employee rehydrate(
            EmployeeId id,
            String employeeCode,
            String fullName,
            EmployeeStatus status
    ) {
        return new Employee(
                id,
                employeeCode,
                fullName,
                status
        );
    }

    public void rename(String newFullName) {
        this.fullName = requireText(newFullName, "fullName");
    }

    public void terminate() {
        if (status == EmployeeStatus.TERMINATED) {
            return;
        }

        this.status = EmployeeStatus.TERMINATED;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return normalized;
    }
}