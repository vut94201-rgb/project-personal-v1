package com.hanyang.identity.identityservicev4mono.organization.domain;

import java.util.Objects;
import java.util.UUID;

public record DepartmentId(UUID value) {
    public DepartmentId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static DepartmentId newId() {
        return new DepartmentId(UUID.randomUUID());
    }
}
