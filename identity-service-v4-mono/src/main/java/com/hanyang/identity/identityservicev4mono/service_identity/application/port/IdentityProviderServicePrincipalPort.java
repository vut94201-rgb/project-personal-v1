package com.hanyang.identity.identityservicev4mono.service_identity.application.port;


/**
 * Provider-neutral boundary for machine/service identity synchronization.
 *
 * <p>The business model knows only that a service principal needs an external
 * non-interactive identity. Keycloak-specific client/service-account types stay
 * in infrastructure.</p>
 */
public interface IdentityProviderServicePrincipalPort {

    ProvisionedServicePrincipal synchronizeServicePrincipal(
            String code,
            String displayName,
            String purpose,
            String externalId,
            boolean enabled
    );

    record ProvisionedServicePrincipal(
            String externalId,
            String externalCode
    ) {
    }
}