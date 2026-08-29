package com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.util.Objects;

public record ServicePrincipalReconciliationResult(
        ServicePrincipalId servicePrincipalId,
        IdentityProviderType provider,
        ServicePrincipalProvisioningStatus status,
        String externalId,
        String externalCode,
        String error
) {

    public ServicePrincipalReconciliationResult {
        Objects.requireNonNull(
                servicePrincipalId,
                "servicePrincipalId must not be null"
        );
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static ServicePrincipalReconciliationResult fromState(
            ServicePrincipalProvisioningState state
    ) {
        return new ServicePrincipalReconciliationResult(
                state.getServicePrincipalId(),
                state.getProvider(),
                state.getStatus(),
                state.getExternalId(),
                state.getExternalCode(),
                state.getLastError()
        );
    }

    public static ServicePrincipalReconciliationResult failed(
            ServicePrincipalId servicePrincipalId,
            IdentityProviderType provider,
            String error
    ) {
        return new ServicePrincipalReconciliationResult(
                servicePrincipalId,
                provider,
                ServicePrincipalProvisioningStatus.FAILED,
                null,
                null,
                error
        );
    }
}