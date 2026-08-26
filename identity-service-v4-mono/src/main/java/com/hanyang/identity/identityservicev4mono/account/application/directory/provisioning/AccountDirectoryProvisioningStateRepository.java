package com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning;


import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;

import java.time.Instant;
import java.util.Optional;

public interface AccountDirectoryProvisioningStateRepository {

    AccountDirectoryProvisioningState requestSynchronization(
            AccountId accountId,
            DirectoryProviderType provider
    );

    AccountDirectoryProvisioningState beginSynchronization(
            AccountId accountId,
            DirectoryProviderType provider
    );

    AccountDirectoryProvisioningState completeSynchronization(
            AccountId accountId,
            DirectoryProviderType provider,
            long synchronizedRevision,
            String externalDn,
            String externalCode,
            Instant synchronizedAt
    );

    AccountDirectoryProvisioningState failSynchronization(
            AccountId accountId,
            DirectoryProviderType provider,
            long attemptedRevision,
            String error
    );

    Optional<AccountDirectoryProvisioningState> findByAccountIdAndProvider(
            AccountId accountId,
            DirectoryProviderType provider
    );
}