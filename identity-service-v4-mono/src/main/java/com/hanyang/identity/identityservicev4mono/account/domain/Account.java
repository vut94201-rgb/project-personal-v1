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
     * Links this business account to the current Keycloak identity.
     *
     * <p>Linking an external identity is deliberately independent from the
     * account lifecycle. A successful provider reconciliation must not, by
     * itself, mean that authentication is allowed.</p>
     */
    public void linkKeycloakSubject(String keycloakSubject) {
        this.keycloakSubject = requireText(keycloakSubject, "keycloakSubject");
    }

    /**
     * Activates a provisioned account.
     *
     * <p>Only ACTIVE accounts are allowed to authenticate. A Keycloak subject
     * must already be linked before activation. DISABLED is terminal in the
     * current lifecycle and therefore cannot be reactivated through this
     * method.</p>
     */
    public void activate() {
        if (status == AccountStatus.ACTIVE) {
            return;
        }

        if (status == AccountStatus.DISABLED) {
            throw new IllegalStateException(
                    "Disabled account cannot be activated"
            );
        }

        if (keycloakSubject == null || keycloakSubject.isBlank()) {
            throw new IllegalStateException(
                    "Account cannot be activated before Keycloak identity is linked"
            );
        }

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