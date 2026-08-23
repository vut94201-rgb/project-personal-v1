package com.hanyang.identity.identityservicev4mono.account.domain;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Account {

    private final AccountId id;
    private final EmployeeId employeeId;

    private final String username;

    private String keycloakSubject;
    private AccountStatus status;

    private Account(
            AccountId id,
            EmployeeId employeeId,
            String username,
            String keycloakSubject,
            AccountStatus status
    ) {
        this.id = Objects.requireNonNull(id);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.username = requireText(username, "username");
        this.keycloakSubject = keycloakSubject;
        this.status = Objects.requireNonNull(status);
    }

    public static Account create(
            AccountId id,
            EmployeeId employeeId,
            String username
    ) {
        return new Account(
                id,
                employeeId,
                username,
                null,
                AccountStatus.PENDING
        );
    }

    public static Account rehydrate(
            AccountId id,
            EmployeeId employeeId,
            String username,
            String keycloakSubject,
            AccountStatus status
    ) {
        return new Account(
                id,
                employeeId,
                username,
                keycloakSubject,
                status
        );
    }

    public void provision(String keycloakSubject) {
        if (status != AccountStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending account can be provisioned"
            );
        }

        this.keycloakSubject =
                requireText(keycloakSubject, "keycloakSubject");

        this.status = AccountStatus.ACTIVE;
    }

    public void disable() {
        if (status == AccountStatus.DISABLED) {
            return;
        }

        this.status = AccountStatus.DISABLED;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        Objects.requireNonNull(
                value,
                fieldName + " must not be null"
        );

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return normalized;
    }
}