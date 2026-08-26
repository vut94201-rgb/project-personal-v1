package com.hanyang.identity.identityservicev4mono.organization.domain;

import java.util.Objects;
import java.util.UUID;

public record PositionId(UUID value) {
    public PositionId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static PositionId newId() {
        return new PositionId(UUID.randomUUID());
    }
}
