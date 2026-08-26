package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.federation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Best-effort bootstrap for infrastructure configuration.
 *
 * <p>Application startup is not coupled to Keycloak availability. Account
 * reconciliation also calls {@link KeycloakLdapFederationManager#requireProviderId()},
 * so a failed startup bootstrap is repaired on the next provisioning attempt.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "integration.keycloak.ldap-federation",
        name = "enabled",
        havingValue = "true"
)
public class KeycloakLdapFederationBootstrap {

    private final KeycloakLdapFederationManager federationManager;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureFederationAfterStartup() {
        try {
            String providerId = federationManager.ensureConfigured();
            log.info("Keycloak LDAP federation is configured. providerId={}", providerId);
        } catch (RuntimeException exception) {
            log.warn(
                    "Keycloak LDAP federation bootstrap failed; provisioning will retry later: {}",
                    exception.getMessage()
            );
        }
    }
}