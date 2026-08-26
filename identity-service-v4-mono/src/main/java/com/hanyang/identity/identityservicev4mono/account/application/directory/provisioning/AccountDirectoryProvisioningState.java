package com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class AccountDirectoryProvisioningState {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final UUID id;
    private final AccountId accountId;
    private final DirectoryProviderType provider;

    private String externalDn;
    private String externalCode;
    private AccountDirectoryProvisioningStatus status;
    private long desiredRevision;
    private long syncedRevision;
    private Instant lastSyncedAt;
    private String lastError;

    private AccountDirectoryProvisioningState(
            UUID id,
            AccountId accountId,
            DirectoryProviderType provider,
            String externalDn,
            String externalCode,
            AccountDirectoryProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.externalDn = normalizeNullable(externalDn);
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

    public static AccountDirectoryProvisioningState pending(
            AccountId accountId,
            DirectoryProviderType provider
    ) {
        return new AccountDirectoryProvisioningState(
                UUID.randomUUID(),
                accountId,
                provider,
                null,
                null,
                AccountDirectoryProvisioningStatus.PENDING,
                1,
                0,
                null,
                null
        );
    }

    public static AccountDirectoryProvisioningState rehydrate(
            UUID id,
            AccountId accountId,
            DirectoryProviderType provider,
            String externalDn,
            String externalCode,
            AccountDirectoryProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        return new AccountDirectoryProvisioningState(
                id,
                accountId,
                provider,
                externalDn,
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
        status = AccountDirectoryProvisioningStatus.PENDING;
        lastError = null;
    }

    public long beginSynchronization() {
        status = AccountDirectoryProvisioningStatus.SYNCING;
        lastError = null;
        return desiredRevision;
    }

    public void markSynchronized(
            long synchronizedRevision,
            String externalDn,
            String externalCode,
            Instant synchronizedAt
    ) {
        validateAttemptRevision(synchronizedRevision);

        if (synchronizedRevision < syncedRevision) {
            return;
        }

        this.externalDn = normalizeNullable(externalDn);
        this.externalCode = normalizeNullable(externalCode);
        this.syncedRevision = synchronizedRevision;
        this.lastSyncedAt = Objects.requireNonNull(
                synchronizedAt,
                "synchronizedAt must not be null"
        );
        this.lastError = null;
        this.status = syncedRevision == desiredRevision
                ? AccountDirectoryProvisioningStatus.SYNCED
                : AccountDirectoryProvisioningStatus.PENDING;
    }

    public void markFailed(long attemptedRevision, String error) {
        validateAttemptRevision(attemptedRevision);

        if (attemptedRevision < syncedRevision) {
            return;
        }

        if (attemptedRevision == desiredRevision) {
            status = AccountDirectoryProvisioningStatus.FAILED;
            lastError = normalizeError(error);
            return;
        }

        status = AccountDirectoryProvisioningStatus.PENDING;
    }

    public void markDrifted(String reason) {
        status = AccountDirectoryProvisioningStatus.DRIFTED;
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