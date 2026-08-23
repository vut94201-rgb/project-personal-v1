package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.util.Objects;

public record RoleReconciliationResult(
        RoleId roleId,
        IdentityProviderType provider,
        RoleProvisioningStatus status,
        String externalId,
        String externalCode,
        String error
) {
    public RoleReconciliationResult {
        Objects.requireNonNull(roleId, "roleId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static RoleReconciliationResult fromState(
            RoleProvisioningState state
    ) {
        return new RoleReconciliationResult(
                state.getRoleId(),
                state.getProvider(),
                state.getStatus(),
                state.getExternalId(),
                state.getExternalCode(),
                state.getLastError()
        );
    }

    public static RoleReconciliationResult failed(
            RoleId roleId,
            IdentityProviderType provider,
            String error
    ) {
        return new RoleReconciliationResult(
                roleId,
                provider,
                RoleProvisioningStatus.FAILED,
                null,
                null,
                error
        );
    }
}