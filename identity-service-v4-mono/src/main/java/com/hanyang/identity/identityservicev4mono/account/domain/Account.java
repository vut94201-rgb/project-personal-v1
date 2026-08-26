package com.hanyang.identity.identityservicev4mono.account.domain;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Account {

    private final AccountId id;
    private final EmployeeId employeeId;
    private final String username;

    private AccountStatus status;

    private Account(
            AccountId id,
            EmployeeId employeeId,
            String username,
            AccountStatus status
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId must not be null");
        this.username = requireText(username, "username");
        this.status = Objects.requireNonNull(status, "status must not be null");
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
                AccountStatus.PENDING
        );
    }

    public static Account rehydrate(
            AccountId id,
            EmployeeId employeeId,
            String username,
            AccountStatus status
    ) {
        return new Account(
                id,
                employeeId,
                username,
                status
        );
    }

    /**
     * Activates an account whose mandatory external provisioning has already
     * been verified by the application-layer activation coordinator.
     *
     * <p>The Account aggregate intentionally contains no Keycloak/LDAP ids.
     * External identity bindings belong to provisioning state, not to the
     * business account itself.</p>
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