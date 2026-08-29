package com.hanyang.identity.identityservicev4mono.service_identity.domain;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicePrincipalOwnerTest {

    @Test
    void createsActiveOwnership() {
        ServicePrincipalOwner owner = ServicePrincipalOwner.create(
                ServicePrincipalOwnerId.newId(),
                ServicePrincipalId.newId(),
                EmployeeId.newId(),
                ServicePrincipalOwnershipType.PRIMARY
        );

        assertEquals(ServicePrincipalOwnerStatus.ACTIVE, owner.getStatus());
        assertNull(owner.getRevokedAt());
    }

    @Test
    void revokeIsIdempotent() {
        ServicePrincipalOwner owner = ServicePrincipalOwner.create(
                ServicePrincipalOwnerId.newId(),
                ServicePrincipalId.newId(),
                EmployeeId.newId(),
                ServicePrincipalOwnershipType.TECHNICAL
        );
        Instant revokedAt = Instant.parse("2026-08-28T09:00:00Z");

        owner.revoke(revokedAt);
        owner.revoke(revokedAt.plusSeconds(60));

        assertEquals(ServicePrincipalOwnerStatus.REVOKED, owner.getStatus());
        assertEquals(revokedAt, owner.getRevokedAt());
    }

    @Test
    void rejectsInvalidRehydratedLifecycle() {
        assertThrows(IllegalArgumentException.class, () ->
                ServicePrincipalOwner.rehydrate(
                        ServicePrincipalOwnerId.newId(),
                        ServicePrincipalId.newId(),
                        EmployeeId.newId(),
                        ServicePrincipalOwnershipType.PRIMARY,
                        ServicePrincipalOwnerStatus.REVOKED,
                        null
                )
        );
    }
}