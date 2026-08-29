package com.hanyang.identity.identityservicev4mono.service_identity.domain;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class ServicePrincipalOwner {

    private final ServicePrincipalOwnerId id;
    private final ServicePrincipalId servicePrincipalId;
    private final EmployeeId employeeId;
    private final ServicePrincipalOwnershipType ownershipType;

    private ServicePrincipalOwnerStatus status;
    private Instant revokedAt;

    private ServicePrincipalOwner(
            ServicePrincipalOwnerId id,
            ServicePrincipalId servicePrincipalId,
            EmployeeId employeeId,
            ServicePrincipalOwnershipType ownershipType,
            ServicePrincipalOwnerStatus status,
            Instant revokedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.servicePrincipalId = Objects.requireNonNull(
                servicePrincipalId,
                "servicePrincipalId must not be null"
        );
        this.employeeId = Objects.requireNonNull(
                employeeId,
                "employeeId must not be null"
        );
        this.ownershipType = Objects.requireNonNull(
                ownershipType,
                "ownershipType must not be null"
        );
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.revokedAt = revokedAt;
        validateLifecycle();
    }

    public static ServicePrincipalOwner create(
            ServicePrincipalOwnerId id,
            ServicePrincipalId servicePrincipalId,
            EmployeeId employeeId,
            ServicePrincipalOwnershipType ownershipType
    ) {
        return new ServicePrincipalOwner(
                id,
                servicePrincipalId,
                employeeId,
                ownershipType,
                ServicePrincipalOwnerStatus.ACTIVE,
                null
        );
    }

    public static ServicePrincipalOwner rehydrate(
            ServicePrincipalOwnerId id,
            ServicePrincipalId servicePrincipalId,
            EmployeeId employeeId,
            ServicePrincipalOwnershipType ownershipType,
            ServicePrincipalOwnerStatus status,
            Instant revokedAt
    ) {
        return new ServicePrincipalOwner(
                id,
                servicePrincipalId,
                employeeId,
                ownershipType,
                status,
                revokedAt
        );
    }

    public void revoke(Instant revokedAt) {
        Objects.requireNonNull(revokedAt, "revokedAt must not be null");

        if (status == ServicePrincipalOwnerStatus.REVOKED) {
            return;
        }

        this.status = ServicePrincipalOwnerStatus.REVOKED;
        this.revokedAt = revokedAt;
    }

    private void validateLifecycle() {
        if (status == ServicePrincipalOwnerStatus.ACTIVE && revokedAt != null) {
            throw new IllegalArgumentException(
                    "active service principal owner must not have revokedAt"
            );
        }

        if (status == ServicePrincipalOwnerStatus.REVOKED && revokedAt == null) {
            throw new IllegalArgumentException(
                    "revoked service principal owner must have revokedAt"
            );
        }
    }
}