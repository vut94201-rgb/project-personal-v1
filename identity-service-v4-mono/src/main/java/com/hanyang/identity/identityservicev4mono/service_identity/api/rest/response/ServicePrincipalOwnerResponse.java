package com.hanyang.identity.identityservicev4mono.service_identity.api.rest.response;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnershipType;

import java.time.Instant;
import java.util.UUID;

public record ServicePrincipalOwnerResponse(
        UUID id,
        UUID employeeId,
        ServicePrincipalOwnershipType ownershipType,
        ServicePrincipalOwnerStatus status,
        Instant revokedAt
) {
}