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
        this.keycloakSubject = normalizeNullable(keycloakSubject);
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

    /**
     * Links the account to the external identity and activates it.
     *
     * <p>The operation is idempotent for an already active account so a
     * reconciliation run can repair an external identifier after an IdP
     * restore without creating a second business account.</p>
     */
    public void provision(String keycloakSubject) {
        if (status == AccountStatus.DISABLED) {
            throw new IllegalStateException(
                    "Disabled account cannot be provisioned"
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

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}