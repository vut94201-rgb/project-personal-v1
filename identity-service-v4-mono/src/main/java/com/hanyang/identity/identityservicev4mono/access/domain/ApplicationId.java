package com.hanyang.identity.identityservicev4mono.access.domain;

import java.util.Objects;
import java.util.UUID;

public record ApplicationId(UUID value) {

    public ApplicationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ApplicationId newId() {
        return new ApplicationId(UUID.randomUUID());
    }
}