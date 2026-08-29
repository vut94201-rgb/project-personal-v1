package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

public record ServicePrincipalRoleReconciliationResult(
        ServicePrincipalId servicePrincipalId,
        RoleId roleId,
        IdentityProviderType provider,
        boolean desiredAssigned,
        ServicePrincipalRoleProvisioningStatus status,
        long desiredRevision,
        long syncedRevision,
        String error
) {
    public static ServicePrincipalRoleReconciliationResult fromState(
            ServicePrincipalRoleProvisioningState state
    ) {
        return new ServicePrincipalRoleReconciliationResult(
                state.getServicePrincipalId(),
                state.getRoleId(),
                state.getProvider(),
                state.isDesiredAssigned(),
                state.getStatus(),
                state.getDesiredRevision(),
                state.getSyncedRevision(),
                state.getLastError()
        );
    }

    public static ServicePrincipalRoleReconciliationResult failed(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned,
            String error
    ) {
        return new ServicePrincipalRoleReconciliationResult(
                servicePrincipalId,
                roleId,
                provider,
                desiredAssigned,
                ServicePrincipalRoleProvisioningStatus.FAILED,
                0,
                0,
                error
        );
    }
}