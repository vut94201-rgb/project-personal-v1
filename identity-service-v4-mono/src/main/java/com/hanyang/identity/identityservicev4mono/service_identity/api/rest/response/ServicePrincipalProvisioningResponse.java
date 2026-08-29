package com.hanyang.identity.identityservicev4mono.service_identity.api.rest.response;

import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.time.Instant;

public record ServicePrincipalProvisioningResponse(
        IdentityProviderType provider,
        ServicePrincipalProvisioningStatus status,
        String externalCode,
        long desiredRevision,
        long syncedRevision,
        Instant lastSyncedAt,
        String lastError
) {
}