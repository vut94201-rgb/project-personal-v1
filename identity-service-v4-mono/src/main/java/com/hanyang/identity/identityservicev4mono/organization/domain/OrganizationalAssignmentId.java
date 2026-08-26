package com.hanyang.identity.identityservicev4mono.organization.domain;

import java.util.Objects;
import java.util.UUID;

public record OrganizationalAssignmentId(UUID value) {
    public OrganizationalAssignmentId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static OrganizationalAssignmentId newId() {
        return new OrganizationalAssignmentId(UUID.randomUUID());
    }
}
