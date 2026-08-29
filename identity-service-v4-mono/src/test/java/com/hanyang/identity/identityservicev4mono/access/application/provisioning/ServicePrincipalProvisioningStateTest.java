package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningState;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ServicePrincipalProvisioningStateTest {

    private static final IdentityProviderType PROVIDER =
            IdentityProviderType.KEYCLOAK;
    private static final Instant SYNC_TIME =
            Instant.parse("2026-08-28T15:00:00Z");

    @Test
    void startsPendingAtFirstDesiredRevision() {
        ServicePrincipalProvisioningState state =
                ServicePrincipalProvisioningState.pending(
                        ServicePrincipalId.newId(),
                        PROVIDER
                );

        assertEquals(
                ServicePrincipalProvisioningStatus.PENDING,
                state.getStatus()
        );
        assertEquals(1, state.getDesiredRevision());
        assertEquals(0, state.getSyncedRevision());
        assertNull(state.getExternalId());
    }

    @Test
    void completionOfOlderRevisionKeepsNewerDesiredStatePending() {
        ServicePrincipalProvisioningState state =
                ServicePrincipalProvisioningState.pending(
                        ServicePrincipalId.newId(),
                        PROVIDER
                );

        long firstAttempt = state.beginSynchronization();
        state.requestSynchronization();

        state.markSynchronized(
                firstAttempt,
                "kc-client-uuid",
                "svc-mes-sync-agent",
                SYNC_TIME
        );

        assertEquals(2, state.getDesiredRevision());
        assertEquals(1, state.getSyncedRevision());
        assertEquals(
                ServicePrincipalProvisioningStatus.PENDING,
                state.getStatus()
        );
        assertEquals("kc-client-uuid", state.getExternalId());
    }

    @Test
    void staleFailureCannotOverrideNewerDesiredRevision() {
        ServicePrincipalProvisioningState state =
                ServicePrincipalProvisioningState.pending(
                        ServicePrincipalId.newId(),
                        PROVIDER
                );

        long firstAttempt = state.beginSynchronization();
        state.requestSynchronization();

        state.markFailed(firstAttempt, "stale provider failure");

        assertEquals(
                ServicePrincipalProvisioningStatus.PENDING,
                state.getStatus()
        );
        assertNull(state.getLastError());
    }

    @Test
    void currentFailureIsRecordedAndErrorIsBounded() {
        ServicePrincipalProvisioningState state =
                ServicePrincipalProvisioningState.pending(
                        ServicePrincipalId.newId(),
                        PROVIDER
                );
        long attempt = state.beginSynchronization();
        String oversized = "x".repeat(2500);

        state.markFailed(attempt, oversized);

        assertEquals(
                ServicePrincipalProvisioningStatus.FAILED,
                state.getStatus()
        );
        assertNotNull(state.getLastError());
        assertEquals(2000, state.getLastError().length());
    }
}