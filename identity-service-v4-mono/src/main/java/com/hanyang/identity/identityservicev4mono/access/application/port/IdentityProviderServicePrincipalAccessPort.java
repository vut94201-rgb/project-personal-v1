package com.hanyang.identity.identityservicev4mono.access.application.port;


/**
 * Provider-neutral boundary for materializing application roles onto a
 * machine/service identity.
 */
public interface IdentityProviderServicePrincipalAccessPort {

    void assignRole(
            String externalServicePrincipalId,
            String applicationCode,
            String roleCode
    );

    void revokeRole(
            String externalServicePrincipalId,
            String applicationCode,
            String roleCode
    );
}