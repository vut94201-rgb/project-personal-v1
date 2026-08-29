package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ServicePrincipalRoleProvisioningState {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final UUID id;
    private final ServicePrincipalId servicePrincipalId;
    private final RoleId roleId;
    private final IdentityProviderType provider;

    private boolean desiredAssigned;
    private ServicePrincipalRoleProvisioningStatus status;
    private long desiredRevision;
    private long syncedRevision;
    private Instant lastSyncedAt;
    private String lastError;

    private ServicePrincipalRoleProvisioningState(
            UUID id,
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned,
            ServicePrincipalRoleProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.servicePrincipalId = Objects.requireNonNull(
                servicePrincipalId,
                "servicePrincipalId must not be null"
        );
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

    public static ServicePrincipalRoleProvisioningState pending(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned
    ) {
        return new ServicePrincipalRoleProvisioningState(
                UUID.randomUUID(),
                servicePrincipalId,
                roleId,
                provider,
                desiredAssigned,
                ServicePrincipalRoleProvisioningStatus.PENDING,
                1,
                0,
                null,
                null
        );
    }

    public static ServicePrincipalRoleProvisioningState rehydrate(
            UUID id,
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned,
            ServicePrincipalRoleProvisioningStatus status,
            long desiredRevision,
            long syncedRevision,
            Instant lastSyncedAt,
            String lastError
    ) {
        return new ServicePrincipalRoleProvisioningState(
                id,
                servicePrincipalId,
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
        status = ServicePrincipalRoleProvisioningStatus.PENDING;
        lastError = null;
    }

    public long beginSynchronization() {
        status = ServicePrincipalRoleProvisioningStatus.SYNCING;
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
                ? ServicePrincipalRoleProvisioningStatus.SYNCED
                : ServicePrincipalRoleProvisioningStatus.PENDING;
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
            status = ServicePrincipalRoleProvisioningStatus.FAILED;
            lastError = normalizeError(error);
            return;
        }

        status = ServicePrincipalRoleProvisioningStatus.PENDING;
    }

    public void markDrifted(String reason) {
        status = ServicePrincipalRoleProvisioningStatus.DRIFTED;
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