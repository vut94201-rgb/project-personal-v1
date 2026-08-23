package com.hanyang.identity.identityservicev4mono.access.domain;


import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

@Getter
public class Application {

    private final ApplicationId id;
    private final String code;

    private String name;
    private ApplicationStatus status;

    private Application(
            ApplicationId id,
            String code,
            String name,
            ApplicationStatus status
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.code = normalizeCode(code);
        this.name = requireText(name, "name");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public static Application create(
            ApplicationId id,
            String code,
            String name
    ) {
        return new Application(
                id,
                code,
                name,
                ApplicationStatus.ACTIVE
        );
    }

    public static Application rehydrate(
            ApplicationId id,
            String code,
            String name,
            ApplicationStatus status
    ) {
        return new Application(
                id,
                code,
                name,
                status
        );
    }

    public void rename(String newName) {
        this.name = requireText(newName, "name");
    }

    public void disable() {
        if (status == ApplicationStatus.DISABLED) {
            return;
        }

        this.status = ApplicationStatus.DISABLED;
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