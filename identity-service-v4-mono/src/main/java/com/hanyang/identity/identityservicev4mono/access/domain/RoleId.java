package com.hanyang.identity.identityservicev4mono.access.domain;


import java.util.Objects;
import java.util.UUID;

public record RoleId(UUID value) {

    public RoleId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static RoleId newId() {
        return new RoleId(UUID.randomUUID());
    }
}