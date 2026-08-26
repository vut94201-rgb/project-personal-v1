package com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning;


import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;

import java.util.Objects;

public record AccountDirectoryReconciliationResult(
        AccountId accountId,
        DirectoryProviderType provider,
        AccountDirectoryProvisioningStatus status,
        String externalDn,
        String externalCode,
        String error
) {
    public AccountDirectoryReconciliationResult {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static AccountDirectoryReconciliationResult fromState(
            AccountDirectoryProvisioningState state
    ) {
        return new AccountDirectoryReconciliationResult(
                state.getAccountId(),
                state.getProvider(),
                state.getStatus(),
                state.getExternalDn(),
                state.getExternalCode(),
                state.getLastError()
        );
    }

    public static AccountDirectoryReconciliationResult failed(
            AccountId accountId,
            DirectoryProviderType provider,
            String error
    ) {
        return new AccountDirectoryReconciliationResult(
                accountId,
                provider,
                AccountDirectoryProvisioningStatus.FAILED,
                null,
                null,
                error
        );
    }
}