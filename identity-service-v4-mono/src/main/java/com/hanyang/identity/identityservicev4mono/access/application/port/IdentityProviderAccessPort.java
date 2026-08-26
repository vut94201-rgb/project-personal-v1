package com.hanyang.identity.identityservicev4mono.access.application.port;

public interface IdentityProviderAccessPort {

    void assignRole(
            String externalUserId,
            String applicationCode,
            String roleCode
    );

    void revokeRole(
            String externalUserId,
            String applicationCode,
            String roleCode
    );
}