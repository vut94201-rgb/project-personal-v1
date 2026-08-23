package com.hanyang.identity.identityservicev4mono.employee.domain;

import java.util.Objects;
import java.util.UUID;

public record EmployeeId(UUID value) {

    public EmployeeId {
        Objects.requireNonNull(value);
    }

    public static EmployeeId newId() {
        return new EmployeeId(UUID.randomUUID());
    }
}