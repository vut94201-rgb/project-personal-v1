package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.time.Instant;
import java.util.Optional;

public interface ApplicationProvisioningStateRepository {

    ApplicationProvisioningState requestSynchronization(
            ApplicationId applicationId,
            IdentityProviderType provider
    );

    ApplicationProvisioningState beginSynchronization(
            ApplicationId applicationId,
            IdentityProviderType provider
    );

    ApplicationProvisioningState completeSynchronization(
            ApplicationId applicationId,
            IdentityProviderType provider,
            long synchronizedRevision,
            String externalId,
            String externalCode,
            Instant synchronizedAt
    );

    ApplicationProvisioningState failSynchronization(
            ApplicationId applicationId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    );

    Optional<ApplicationProvisioningState> findByApplicationIdAndProvider(
            ApplicationId applicationId,
            IdentityProviderType provider
    );
}