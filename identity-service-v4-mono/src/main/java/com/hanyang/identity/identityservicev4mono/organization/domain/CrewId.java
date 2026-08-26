package com.hanyang.identity.identityservicev4mono.organization.domain;


import java.util.Objects;
import java.util.UUID;

public record CrewId(UUID value) {
    public CrewId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static CrewId newId() {
        return new CrewId(UUID.randomUUID());
    }
}