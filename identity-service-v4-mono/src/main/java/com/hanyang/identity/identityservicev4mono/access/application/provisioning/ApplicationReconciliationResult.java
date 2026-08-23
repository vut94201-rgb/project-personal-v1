package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.util.Objects;

public record ApplicationReconciliationResult(
        ApplicationId applicationId,
        IdentityProviderType provider,
        ApplicationProvisioningStatus status,
        String externalId,
        String externalCode,
        String error
) {
    public ApplicationReconciliationResult {
        Objects.requireNonNull(applicationId, "applicationId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static ApplicationReconciliationResult fromState(
            ApplicationProvisioningState state
    ) {
        return new ApplicationReconciliationResult(
                state.getApplicationId(),
                state.getProvider(),
                state.getStatus(),
                state.getExternalId(),
                state.getExternalCode(),
                state.getLastError()
        );
    }

    public static ApplicationReconciliationResult failed(
            ApplicationId applicationId,
            IdentityProviderType provider,
            String error
    ) {
        return new ApplicationReconciliationResult(
                applicationId,
                provider,
                ApplicationProvisioningStatus.FAILED,
                null,
                null,
                error
        );
    }
}