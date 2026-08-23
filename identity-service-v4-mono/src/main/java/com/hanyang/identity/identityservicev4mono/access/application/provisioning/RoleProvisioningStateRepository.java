package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.time.Instant;
import java.util.Optional;

public interface RoleProvisioningStateRepository {

    RoleProvisioningState requestSynchronization(
            RoleId roleId,
            IdentityProviderType provider
    );

    RoleProvisioningState beginSynchronization(
            RoleId roleId,
            IdentityProviderType provider
    );

    RoleProvisioningState completeSynchronization(
            RoleId roleId,
            IdentityProviderType provider,
            long synchronizedRevision,
            String externalId,
            String externalCode,
            Instant synchronizedAt
    );

    RoleProvisioningState failSynchronization(
            RoleId roleId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    );

    Optional<RoleProvisioningState> findByRoleIdAndProvider(
            RoleId roleId,
            IdentityProviderType provider
    );
}