package com.hanyang.identity.identityservicev4mono.employee.domain;


import java.util.Objects;
import java.util.UUID;

public record EmployeeNationalIdentityId(UUID value) {

    public EmployeeNationalIdentityId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static EmployeeNationalIdentityId newId() {
        return new EmployeeNationalIdentityId(UUID.randomUUID());
    }
}