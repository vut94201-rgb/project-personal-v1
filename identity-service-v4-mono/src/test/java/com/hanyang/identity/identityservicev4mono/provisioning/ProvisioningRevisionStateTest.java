package com.hanyang.identity.identityservicev4mono.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.provisioning.*;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ProvisioningRevisionStateTest {

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;
    private static final Instant SYNC_TIME = Instant.parse("2026-08-25T04:30:00Z");

    @Test
    void applicationCompletionFromOlderRevisionKeepsNewerDesiredStatePending() {
        ApplicationProvisioningState state = ApplicationProvisioningState.pending(
                ApplicationId.newId(),
                PROVIDER
        );

        long firstAttempt = state.beginSynchronization();
        state.requestSynchronization();
        state.requestSynchronization();

        state.markSynchronized(firstAttempt, "client-uuid", "oqc", SYNC_TIME);

        assertEquals(3, state.getDesiredRevision());
        assertEquals(1, state.getSyncedRevision());
        assertEquals(ApplicationProvisioningStatus.PENDING, state.getStatus());
        assertEquals("client-uuid", state.getExternalId());
    }

    @Test
    void roleFailureFromOlderRevisionCannotTurnNewerDesiredStateFailed() {
        RoleProvisioningState state = RoleProvisioningState.pending(RoleId.newId(), PROVIDER);

        long firstAttempt = state.beginSynchronization();
        state.requestSynchronization();

        state.markFailed(firstAttempt, "stale failure");

        assertEquals(2, state.getDesiredRevision());
        assertEquals(0, state.getSyncedRevision());
        assertEquals(RoleProvisioningStatus.PENDING, state.getStatus());
        assertNull(state.getLastError());
    }

    @Test
    void accountCompletionFromOlderRevisionCannotHidePendingDisable() {
        AccountProvisioningState state = AccountProvisioningState.pending(
                AccountId.newId(),
                PROVIDER
        );

        long firstAttempt = state.beginSynchronization();
        state.requestSynchronization();

        state.markSynchronized(firstAttempt, "subject-1", "employee01", SYNC_TIME);

        assertEquals(2, state.getDesiredRevision());
        assertEquals(1, state.getSyncedRevision());
        assertEquals(AccountProvisioningStatus.PENDING, state.getStatus());
    }

    @Test
    void accountRoleAssignThenRevokePreservesLatestDesiredStateAcrossOldCompletion() {
        AccountRoleProvisioningState state = AccountRoleProvisioningState.pending(
                AccountId.newId(),
                RoleId.newId(),
                PROVIDER,
                true
        );

        long assignAttempt = state.beginSynchronization();
        state.requestSynchronization(false);

        state.markSynchronized(assignAttempt, SYNC_TIME);

        assertFalse(state.isDesiredAssigned());
        assertEquals(2, state.getDesiredRevision());
        assertEquals(1, state.getSyncedRevision());
        assertEquals(AccountRoleProvisioningStatus.PENDING, state.getStatus());
    }
}