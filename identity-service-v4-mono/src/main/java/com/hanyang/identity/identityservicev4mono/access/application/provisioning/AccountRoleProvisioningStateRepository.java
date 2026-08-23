package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.time.Instant;
import java.util.Optional;

public interface AccountRoleProvisioningStateRepository {

    AccountRoleProvisioningState requestSynchronization(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned
    );

    AccountRoleProvisioningState beginSynchronization(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider
    );

    AccountRoleProvisioningState completeSynchronization(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            long synchronizedRevision,
            Instant synchronizedAt
    );

    AccountRoleProvisioningState failSynchronization(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    );

    Optional<AccountRoleProvisioningState> findByKeyAndProvider(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider
    );
}