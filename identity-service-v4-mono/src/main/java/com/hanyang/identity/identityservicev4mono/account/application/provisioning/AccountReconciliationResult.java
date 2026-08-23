package com.hanyang.identity.identityservicev4mono.account.application.provisioning;


import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.util.Objects;

public record AccountReconciliationResult(
        AccountId accountId,
        IdentityProviderType provider,
        AccountProvisioningStatus status,
        String externalId,
        String externalCode,
        String error
) {
    public AccountReconciliationResult {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static AccountReconciliationResult fromState(
            AccountProvisioningState state
    ) {
        return new AccountReconciliationResult(
                state.getAccountId(),
                state.getProvider(),
                state.getStatus(),
                state.getExternalId(),
                state.getExternalCode(),
                state.getLastError()
        );
    }

    public static AccountReconciliationResult failed(
            AccountId accountId,
            IdentityProviderType provider,
            String error
    ) {
        return new AccountReconciliationResult(
                accountId,
                provider,
                AccountProvisioningStatus.FAILED,
                null,
                null,
                error
        );
    }
}