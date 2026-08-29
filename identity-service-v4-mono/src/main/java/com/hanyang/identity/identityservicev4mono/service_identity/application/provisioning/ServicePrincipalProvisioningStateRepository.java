package com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.time.Instant;
import java.util.Optional;

public interface ServicePrincipalProvisioningStateRepository {

    ServicePrincipalProvisioningState requestSynchronization(
            ServicePrincipalId servicePrincipalId,
            IdentityProviderType provider
    );

    ServicePrincipalProvisioningState beginSynchronization(
            ServicePrincipalId servicePrincipalId,
            IdentityProviderType provider
    );

    ServicePrincipalProvisioningState completeSynchronization(
            ServicePrincipalId servicePrincipalId,
            IdentityProviderType provider,
            long synchronizedRevision,
            String externalId,
            String externalCode,
            Instant synchronizedAt
    );

    ServicePrincipalProvisioningState failSynchronization(
            ServicePrincipalId servicePrincipalId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    );

    Optional<ServicePrincipalProvisioningState> findByServicePrincipalIdAndProvider(
            ServicePrincipalId servicePrincipalId,
            IdentityProviderType provider
    );
}