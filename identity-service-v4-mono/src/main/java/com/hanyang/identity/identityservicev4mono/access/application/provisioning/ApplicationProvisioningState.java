package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ApplicationProvisioningState {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final UUID id;
    private final ApplicationId applicationId;
    private final IdentityProviderType provider;

    private String externalId;
    private String externalCode;
    private ApplicationProvisioningStatus status;
    private long desiredRevision;
    private long syncedRevision;
    private Instant lastSyncedAt;
    private String lastError;

    private ApplicationProvisioningState(
            UUID id,
            ApplicationId applicationId,
            IdentityProviderType provider,
            String externalId,
            String externalCode,
            ApplicationProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.applicationId = Objects.requireNonNull(applicationId, "applicationId must not be null");
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.externalId = normalizeNullable(externalId);
        this.externalCode = normalizeNullable(externalCode);
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.desiredRevision = requireNonNegative(desiredRevision, "desiredRevision");
        this.syncedRevision = requireNonNegative(syncedRevision, "syncedRevision");
        this.lastSyncedAt = lastSyncedAt;
        this.lastError = normalizeError(lastError);

        if (this.syncedRevision > this.desiredRevision) {
            throw new IllegalArgumentException("syncedRevision must not be greater than desiredRevision");
        }
    }

    public static ApplicationProvisioningState pending(
            ApplicationId applicationId,
            IdentityProviderType provider
    ) {
        return new ApplicationProvisioningState(
                UUID.randomUUID(),
                applicationId,
                provider,
                null,
                null,
                ApplicationProvisioningStatus.PENDING,
                1,
                0,
                null,
                null
        );
    }

    public static ApplicationProvisioningState rehydrate(
            UUID id,
            ApplicationId applicationId,
            IdentityProviderType provider,
            String externalId,
            String externalCode,
            ApplicationProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        return new ApplicationProvisioningState(
                id,
                applicationId,
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
        status = ApplicationProvisioningStatus.PENDING;
        lastError = null;
    }

    public long beginSynchronization() {
        status = ApplicationProvisioningStatus.SYNCING;
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

        this.externalId = requireText(externalId, "externalId");
        this.externalCode = requireText(externalCode, "externalCode");
        this.syncedRevision = synchronizedRevision;
        this.lastSyncedAt = Objects.requireNonNull(synchronizedAt, "synchronizedAt must not be null");
        this.lastError = null;
        this.status = syncedRevision == desiredRevision
                ? ApplicationProvisioningStatus.SYNCED
                : ApplicationProvisioningStatus.PENDING;
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
            status = ApplicationProvisioningStatus.FAILED;
            lastError = normalizeError(error);
            return;
        }

        status = ApplicationProvisioningStatus.PENDING;
    }

    public void markDrifted(String reason) {
        status = ApplicationProvisioningStatus.DRIFTED;
        lastError = normalizeError(reason);
    }

    private void validateAttemptRevision(long revision) {
        requireNonNegative(revision, "revision");

        if (revision > desiredRevision) {
            throw new IllegalArgumentException("revision must not be greater than desiredRevision");
        }
    }

    private static long requireNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
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