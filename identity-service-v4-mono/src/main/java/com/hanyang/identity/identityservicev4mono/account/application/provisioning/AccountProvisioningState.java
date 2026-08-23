package com.hanyang.identity.identityservicev4mono.account.application.provisioning;


import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class AccountProvisioningState {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final UUID id;
    private final AccountId accountId;
    private final IdentityProviderType provider;

    private String externalId;
    private String externalCode;
    private AccountProvisioningStatus status;
    private long desiredRevision;
    private long syncedRevision;
    private Instant lastSyncedAt;
    private String lastError;

    private AccountProvisioningState(
            UUID id,
            AccountId accountId,
            IdentityProviderType provider,
            String externalId,
            String externalCode,
            AccountProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.externalId = normalizeNullable(externalId);
        this.externalCode = normalizeNullable(externalCode);
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

    public static AccountProvisioningState pending(
            AccountId accountId,
            IdentityProviderType provider
    ) {
        return new AccountProvisioningState(
                UUID.randomUUID(),
                accountId,
                provider,
                null,
                null,
                AccountProvisioningStatus.PENDING,
                1,
                0,
                null,
                null
        );
    }

    public static AccountProvisioningState rehydrate(
            UUID id,
            AccountId accountId,
            IdentityProviderType provider,
            String externalId,
            String externalCode,
            AccountProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        return new AccountProvisioningState(
                id,
                accountId,
                provider,
                externalId,
                externalCode,
                status,
                desiredRevision,
                syncedRevision,
                lastSyncedAt,
                lastError
        );
    }

    public void requestSynchronization() {
        desiredRevision = Math.incrementExact(desiredRevision);
        status = AccountProvisioningStatus.PENDING;
        lastError = null;
    }

    public long beginSynchronization() {
        status = AccountProvisioningStatus.SYNCING;
        lastError = null;
        return desiredRevision;
    }

    public void markSynchronized(
            long synchronizedRevision,
            String externalId,
            String externalCode,
            Instant synchronizedAt
    ) {
        validateAttemptRevision(synchronizedRevision);

        if (synchronizedRevision < syncedRevision) {
            return;
        }

        this.externalId = normalizeNullable(externalId);
        this.externalCode = normalizeNullable(externalCode);
        this.syncedRevision = synchronizedRevision;
        this.lastSyncedAt = Objects.requireNonNull(
                synchronizedAt,
                "synchronizedAt must not be null"
        );
        this.lastError = null;
        this.status = syncedRevision == desiredRevision
                ? AccountProvisioningStatus.SYNCED
                : AccountProvisioningStatus.PENDING;
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
            status = AccountProvisioningStatus.FAILED;
            lastError = normalizeError(error);
            return;
        }

        status = AccountProvisioningStatus.PENDING;
    }

    public void markDrifted(String reason) {
        status = AccountProvisioningStatus.DRIFTED;
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