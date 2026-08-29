package com.hanyang.identity.identityservicev4mono.service_identity.api.rest.response;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;

import java.util.List;
import java.util.UUID;

public record ServicePrincipalResponse(
        UUID id,
        String code,
        String displayName,
        String purpose,
        String description,
        ServicePrincipalStatus status,
        ServicePrincipalProvisioningResponse provisioning,
        List<ServicePrincipalOwnerResponse> activeOwners
) {
}