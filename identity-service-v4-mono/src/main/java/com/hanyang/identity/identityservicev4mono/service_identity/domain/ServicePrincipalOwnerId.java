package com.hanyang.identity.identityservicev4mono.service_identity.domain;

import java.util.Objects;
import java.util.UUID;


public record ServicePrincipalOwnerId(UUID value) {

    public ServicePrincipalOwnerId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ServicePrincipalOwnerId newId() {
        return new ServicePrincipalOwnerId(UUID.randomUUID());
    }
}