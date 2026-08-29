package com.hanyang.identity.identityservicev4mono.service_identity.domain;

import java.util.Objects;
import java.util.UUID;

public record ServicePrincipalId(UUID value) {

    public ServicePrincipalId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ServicePrincipalId newId() {
        return new ServicePrincipalId(UUID.randomUUID());
    }
}