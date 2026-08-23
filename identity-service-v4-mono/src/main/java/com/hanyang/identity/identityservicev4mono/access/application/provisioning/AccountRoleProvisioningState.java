package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class AccountRoleProvisioningState {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final UUID id;
    private final AccountId accountId;
    private final RoleId roleId;
    private final IdentityProviderType provider;

    private boolean desiredAssigned;
    private AccountRoleProvisioningStatus status;
    private long desiredRevision;
    private long syncedRevision;
    private Instant lastSyncedAt;
    private String lastError;

    private AccountRoleProvisioningState(
            UUID id,
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned,
            AccountRoleProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.desiredAssigned = desiredAssigned;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.desiredRevision = requireNonNegative(desiredRevision, "desiredRevision");
        this.syncedRevision = requireNonNegative(syncedRevision, "syncedRevision");
        this.lastSyncedAt = lastSyncedAt;
        this.lastError = normalizeError(lastError);

        if (this.syncedRevision > this.desiredRevision) {
            throw new IllegalArgumentException(
                    "syncedRevision must not be greater than desiredRevision"
            );
        }
    }

    public static AccountRoleProvisioningState pending(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned
    ) {
        return new AccountRoleProvisioningState(
                UUID.randomUUID(),
                accountId,
                roleId,
                provider,
                desiredAssigned,
                AccountRoleProvisioningStatus.PENDING,
                1,
                0,
                null,
                null
        );
    }

    public static AccountRoleProvisioningState rehydrate(
            UUID id,
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned,
            AccountRoleProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        return new AccountRoleProvisioningState(
                id,
                accountId,
                roleId,
                provider,
                desiredAssigned,
                status,
                desiredRevision,
                syncedRevision,
                lastSyncedAt,
                lastError
        );
    }

    public void requestSynchronization(boolean desiredAssigned) {
        this.desiredAssigned = desiredAssigned;
        desiredRevision = Math.incrementExact(desiredRevision);
        status = AccountRoleProvisioningStatus.PENDING;
        lastError = null;
    }

    public long beginSynchronization() {
        status = AccountRoleProvisioningStatus.SYNCING;
        lastError = null;
        return desiredRevision;
    }

    public void markSynchronized(
            long synchronizedRevision,
            Instant synchronizedAt
    ) {
        validateAttemptRevision(synchronizedRevision);

        if (synchronizedRevision < syncedRevision) {
            return;
        }

        syncedRevision = synchronizedRevision;
        lastSyncedAt = Objects.requireNonNull(
                synchronizedAt,
                "synchronizedAt must not be null"
        );
        lastError = null;
        status = syncedRevision == desiredRevision
                ? AccountRoleProvisioningStatus.SYNCED
                : AccountRoleProvisioningStatus.PENDING;
    }

    public void markFailed(
            long attemptedRevision,
            String error
    ) {
        validateAttemptRevision(attemptedRevision);

        if (attemptedRevision < syncedRevision) {
            return;
        }

        if (attemptedRevision == desiredRevision) {
            status = AccountRoleProvisioningStatus.FAILED;
            lastError = normalizeError(error);
            return;
        }

        status = AccountRoleProvisioningStatus.PENDING;
    }

    public void markDrifted(String reason) {
        status = AccountRoleProvisioningStatus.DRIFTED;
        lastError = normalizeError(reason);
    }

    private void validateAttemptRevision(long revision) {
        requireNonNegative(revision, "revision");
        if (revision > desiredRevision) {
            throw new IllegalArgumentException(
                    "revision must not be greater than desiredRevision"
            );
        }
    }

    private static long requireNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeError(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null || normalized.length() <= MAX_ERROR_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_ERROR_LENGTH);
    }
}