package com.hanyang.identity.identityservicev4mono.account.application.port;

public interface IdentityProviderAccountPort {

    ProvisionedAccount ensureAccount(
            String username,
            String externalId,
            boolean enabled
    );

    ProvisionedAccount disableAccount(
            String username,
            String externalId
    );

    record ProvisionedAccount(
            String externalId,
            String externalCode
    ) {
    }
}