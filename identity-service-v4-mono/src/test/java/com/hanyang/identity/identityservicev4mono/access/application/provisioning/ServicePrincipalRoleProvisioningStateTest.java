package com.hanyang.identity.identityservicev4mono.access.application.provisioning;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ServicePrincipalRoleProvisioningStateTest {

    @Test
    void staleCompletionDoesNotOverwriteNewerDesiredRevision() {
        ServicePrincipalRoleProvisioningState state =
                ServicePrincipalRoleProvisioningState.pending(
                        ServicePrincipalId.newId(),
                        RoleId.newId(),
                        IdentityProviderType.KEYCLOAK,
                        true
                );

        long firstAttempt = state.beginSynchronization();
        state.requestSynchronization(false);

        state.markSynchronized(firstAttempt, Instant.parse("2026-08-28T00:00:00Z"));

        assertEquals(2, state.getDesiredRevision());
        assertEquals(1, state.getSyncedRevision());
        assertFalse(state.isDesiredAssigned());
        assertEquals(
                ServicePrincipalRoleProvisioningStatus.PENDING,
                state.getStatus()
        );
    }

    @Test
    void currentFailureBecomesFailed() {
        ServicePrincipalRoleProvisioningState state =
                ServicePrincipalRoleProvisioningState.pending(
                        ServicePrincipalId.newId(),
                        RoleId.newId(),
                        IdentityProviderType.KEYCLOAK,
                        true
                );

        long attempt = state.beginSynchronization();
        state.markFailed(attempt, "Keycloak unavailable");

        assertEquals(
                ServicePrincipalRoleProvisioningStatus.FAILED,
                state.getStatus()
        );
        assertEquals("Keycloak unavailable", state.getLastError());
    }
}