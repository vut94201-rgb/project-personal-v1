package com.hanyang.identity.identityservicev4mono.access.api.rest.response;


import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleStatus;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.time.Instant;
import java.util.UUID;

public record ServicePrincipalRoleResponse(
        UUID roleId,
        UUID applicationId,
        String code,
        String name,
        RoleStatus roleStatus,
        IdentityProviderType provider,
        ServicePrincipalRoleProvisioningStatus provisioningStatus,
        long desiredRevision,
        long syncedRevision,
        Instant lastSyncedAt,
        String lastError
) {
}