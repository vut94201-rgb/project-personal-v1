package com.hanyang.identity.identityservicev4mono.organization.domain;


import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

@Getter
public class Crew {
    private final CrewId id;
    private final DepartmentId departmentId;
    private final String code;
    private String name;
    private OrganizationReferenceStatus status;

    private Crew(
            CrewId id,
            DepartmentId departmentId,
            String code,
            String name,
            OrganizationReferenceStatus status
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.departmentId = Objects.requireNonNull(departmentId, "departmentId must not be null");
        this.code = normalizeCode(code);
        this.name = requireText(name, "name");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public static Crew create(CrewId id, DepartmentId departmentId, String code, String name) {
        return new Crew(id, departmentId, code, name, OrganizationReferenceStatus.ACTIVE);
    }

    public static Crew rehydrate(
            CrewId id,
            DepartmentId departmentId,
            String code,
            String name,
            OrganizationReferenceStatus status
    ) {
        return new Crew(id, departmentId, code, name, status);
    }

    public void rename(String newName) {
        this.name = requireText(newName, "name");
    }

    public void disable() {
        this.status = OrganizationReferenceStatus.DISABLED;
    }

    private static String normalizeCode(String value) {
        String normalized = requireText(value, "code").toUpperCase(Locale.ROOT);
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
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}