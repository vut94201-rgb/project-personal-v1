package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;

import java.util.Objects;

public record AccountRoleReconciliationResult(
        AccountId accountId,
        RoleId roleId,
        IdentityProviderType provider,
        AccountRoleProvisioningStatus status,
        boolean desiredAssigned,
        String error
) {
    public AccountRoleReconciliationResult {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(roleId, "roleId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static AccountRoleReconciliationResult fromState(
            AccountRoleProvisioningState state
    ) {
        return new AccountRoleReconciliationResult(
                state.getAccountId(),
                state.getRoleId(),
                state.getProvider(),
                state.getStatus(),
                state.isDesiredAssigned(),
                state.getLastError()
        );
    }

    public static AccountRoleReconciliationResult failed(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned,
            String error
    ) {
        return new AccountRoleReconciliationResult(
                accountId,
                roleId,
                provider,
                AccountRoleProvisioningStatus.FAILED,
                desiredAssigned,
                error
        );
    }
}