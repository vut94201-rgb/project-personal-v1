package com.hanyang.identity.identityservicev4mono.account.application.provisioning;


import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.time.Instant;
import java.util.Optional;

public interface AccountProvisioningStateRepository {

    AccountProvisioningState requestSynchronization(
            AccountId accountId,
            IdentityProviderType provider
    );

    AccountProvisioningState beginSynchronization(
            AccountId accountId,
            IdentityProviderType provider
    );

    AccountProvisioningState completeSynchronization(
            AccountId accountId,
            IdentityProviderType provider,
            long synchronizedRevision,
            String externalId,
            String externalCode,
            Instant synchronizedAt
    );

    AccountProvisioningState failSynchronization(
            AccountId accountId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    );

    Optional<AccountProvisioningState> findByAccountIdAndProvider(
            AccountId accountId,
            IdentityProviderType provider
    );
}