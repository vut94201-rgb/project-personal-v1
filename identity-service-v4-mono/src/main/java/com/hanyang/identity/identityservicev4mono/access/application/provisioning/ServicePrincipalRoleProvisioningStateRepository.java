package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.time.Instant;
import java.util.Optional;

public interface ServicePrincipalRoleProvisioningStateRepository {

    ServicePrincipalRoleProvisioningState requestSynchronization(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned
    );

    ServicePrincipalRoleProvisioningState beginSynchronization(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider
    );

    ServicePrincipalRoleProvisioningState completeSynchronization(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            long synchronizedRevision,
            Instant synchronizedAt
    );

    ServicePrincipalRoleProvisioningState failSynchronization(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    );

    Optional<ServicePrincipalRoleProvisioningState> findByKeyAndProvider(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider
    );
}