package com.hanyang.identity.identityservicev4mono.service_identity.domain;


import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

@Getter
public class ServicePrincipal {

    private final ServicePrincipalId id;
    private final String code;

    private String displayName;
    private String purpose;
    private String description;
    private ServicePrincipalStatus status;

    private ServicePrincipal(
            ServicePrincipalId id,
            String code,
            String displayName,
            String purpose,
            String description,
            ServicePrincipalStatus status
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.code = normalizeCode(code);
        this.displayName = requireText(displayName, "displayName");
        this.purpose = requireText(purpose, "purpose");
        this.description = normalizeOptionalText(description);
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public static ServicePrincipal create(
            ServicePrincipalId id,
            String code,
            String displayName,
            String purpose,
            String description
    ) {
        return new ServicePrincipal(
                id,
                code,
                displayName,
                purpose,
                description,
                ServicePrincipalStatus.PENDING
        );
    }

    public static ServicePrincipal rehydrate(
            ServicePrincipalId id,
            String code,
            String displayName,
            String purpose,
            String description,
            ServicePrincipalStatus status
    ) {
        return new ServicePrincipal(
                id,
                code,
                displayName,
                purpose,
                description,
                status
        );
    }

    public void updateDetails(
            String displayName,
            String purpose,
            String description
    ) {
        this.displayName = requireText(displayName, "displayName");
        this.purpose = requireText(purpose, "purpose");
        this.description = normalizeOptionalText(description);
    }

    /**
     * Activates a service principal after mandatory external provisioning has
     * been verified by the application-layer lifecycle coordinator.
     */
    public void activate() {
        if (status == ServicePrincipalStatus.ACTIVE) {
            return;
        }

        if (status == ServicePrincipalStatus.DISABLED) {
            throw new IllegalStateException(
                    "Disabled service principal cannot be activated"
            );
        }

        this.status = ServicePrincipalStatus.ACTIVE;
    }

    public void disable() {
        if (status == ServicePrincipalStatus.DISABLED) {
            return;
        }

        this.status = ServicePrincipalStatus.DISABLED;
    }

    private static String normalizeCode(String value) {
        String normalized = requireText(value, "code")
                .toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException(
                    "code must start with A-Z and contain only A-Z, 0-9 or underscore"
            );
        }

        return normalized;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return normalized;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}