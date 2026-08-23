package com.hanyang.identity.identityservicev4mono.access.domain;


import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

@Getter
public class Role {

    private final RoleId id;
    private final ApplicationId applicationId;
    private final String code;

    private String name;
    private RoleStatus status;

    private Role(
            RoleId id,
            ApplicationId applicationId,
            String code,
            String name,
            RoleStatus status
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.applicationId = Objects.requireNonNull(
                applicationId,
                "applicationId must not be null"
        );
        this.code = normalizeCode(code);
        this.name = requireText(name, "name");
        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );
    }

    public static Role create(
            RoleId id,
            ApplicationId applicationId,
            String code,
            String name
    ) {
        return new Role(
                id,
                applicationId,
                code,
                name,
                RoleStatus.ACTIVE
        );
    }

    public static Role rehydrate(
            RoleId id,
            ApplicationId applicationId,
            String code,
            String name,
            RoleStatus status
    ) {
        return new Role(
                id,
                applicationId,
                code,
                name,
                status
        );
    }

    public void rename(String newName) {
        this.name = requireText(newName, "name");
    }

    public void disable() {
        if (status == RoleStatus.DISABLED) {
            return;
        }

        this.status = RoleStatus.DISABLED;
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