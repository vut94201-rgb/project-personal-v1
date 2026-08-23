package com.hanyang.identity.identityservicev4mono.access.domain;

import java.util.Objects;
import java.util.UUID;

public record PermissionId(UUID value) {

            public PermissionId {
                Objects.requireNonNull(value, "value must not be null");
            }

            public static PermissionId newId() {
                return new PermissionId(UUID.randomUUID());
            }
}