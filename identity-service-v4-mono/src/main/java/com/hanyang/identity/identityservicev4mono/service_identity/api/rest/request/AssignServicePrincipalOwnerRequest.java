package com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnershipType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignServicePrincipalOwnerRequest(
        @NotNull UUID employeeId,
        @NotNull ServicePrincipalOwnershipType ownershipType
) {
}