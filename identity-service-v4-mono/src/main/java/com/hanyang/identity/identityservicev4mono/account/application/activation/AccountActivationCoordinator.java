package com.hanyang.identity.identityservicev4mono.account.application.activation;


import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;


@Service
public class AccountActivationCoordinator {

    private static final DirectoryProviderType DIRECTORY_PROVIDER =
            DirectoryProviderType.DS389;
    private static final IdentityProviderType IDENTITY_PROVIDER =
            IdentityProviderType.KEYCLOAK;

    private final AccountRepository accountRepository;
    private final AccountDirectoryProvisioningStateRepository directoryStateRepository;
    private final AccountProvisioningStateRepository identityProviderStateRepository;
    private final OutboxPublisher outboxPublisher;

    public AccountActivationCoordinator(
            AccountRepository accountRepository,
            AccountDirectoryProvisioningStateRepository directoryStateRepository,
            AccountProvisioningStateRepository identityProviderStateRepository,
            OutboxPublisher outboxPublisher
    ) {
        this.accountRepository = accountRepository;
        this.directoryStateRepository = directoryStateRepository;
        this.identityProviderStateRepository = identityProviderStateRepository;
        this.outboxPublisher = outboxPublisher;
    }

    @Transactional
    public void afterDirectorySynchronization(AccountId accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (!isPending(account)) {
            return;
        }

        AccountDirectoryProvisioningState directoryState = directoryStateRepository
                .findByAccountIdAndProvider(accountId, DIRECTORY_PROVIDER)
                .orElse(null);
        if (!isCurrent(directoryState)) {
            return;
        }

        Optional<AccountProvisioningState> identityProviderState =
                identityProviderStateRepository.findByAccountIdAndProvider(
                        accountId,
                        IDENTITY_PROVIDER
                );

        if (identityProviderState.isEmpty()) {
            requestIdentityProviderSynchronization(accountId);
            return;
        }

        AccountProvisioningState providerState = identityProviderState.get();
        if (providerState.getStatus() == AccountProvisioningStatus.FAILED
                || providerState.getStatus() == AccountProvisioningStatus.DRIFTED) {

            requestIdentityProviderSynchronization(accountId);
            return;
        }

        if (!isCurrent(providerState)) {
            // PENDING/SYNCING already have work in flight. Do not create a
            // duplicate desired revision/outbox event.
            return;
        }

        // A previously-SYNCED Keycloak binding may pre-date the fresh LDAP
        // identity (for example after migration from local Keycloak users).
        // Force federation reconciliation once more before activation.
        if (providerNeedsRefreshAfterDirectory(directoryState, providerState)
                || !hasExternalBinding(providerState)) {
            requestIdentityProviderSynchronization(accountId);
            return;
        }

        activateAndScheduleEnforcement(account, directoryState, providerState);
    }

    /**
     * Called after a successful Keycloak federation reconciliation.
     */
    @Transactional
    public void afterIdentityProviderSynchronization(AccountId accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (!isPending(account)) {
            return;
        }

        AccountDirectoryProvisioningState directoryState = directoryStateRepository
                .findByAccountIdAndProvider(accountId, DIRECTORY_PROVIDER)
                .orElse(null);
        AccountProvisioningState providerState = identityProviderStateRepository
                .findByAccountIdAndProvider(accountId, IDENTITY_PROVIDER)
                .orElse(null);

        if (!isCurrent(directoryState) || !isCurrent(providerState)) {
            return;
        }

        if (providerNeedsRefreshAfterDirectory(directoryState, providerState)) {
            requestIdentityProviderSynchronization(accountId);
            return;
        }

        if (!hasExternalBinding(providerState)) {
            throw new IllegalStateException(
                    "Keycloak provisioning is SYNCED but has no external binding. accountId="
                            + accountId.value()
            );
        }

        activateAndScheduleEnforcement(account, directoryState, providerState);
    }

    private void activateAndScheduleEnforcement(
            Account account,
            AccountDirectoryProvisioningState directoryState,
            AccountProvisioningState providerState
    ) {
        if (!isCurrent(directoryState)
                || !isCurrent(providerState)
                || !hasExternalBinding(providerState)) {
            return;
        }

        account.activate();
        accountRepository.save(account);

        // Both external systems were synchronized while the account was
        // PENDING, therefore LDAP is still locked and Keycloak is disabled.
        // ACTIVE is now the source of truth, so publish a fresh desired
        // revision to converge both enforcement targets to enabled/unlocked.
        requestDirectorySynchronization(account.getId());
        requestIdentityProviderSynchronization(account.getId());
    }

    private void requestDirectorySynchronization(AccountId accountId) {
        directoryStateRepository.requestSynchronization(
                accountId,
                DIRECTORY_PROVIDER
        );
        outboxPublisher.publish(
                AccountDirectoryProvisioningService.OUTBOX_AGGREGATE_TYPE,
                accountId.value().toString(),
                AccountDirectoryProvisioningService.OUTBOX_EVENT_TYPE,
                null
        );
    }

    private void requestIdentityProviderSynchronization(AccountId accountId) {
        identityProviderStateRepository.requestSynchronization(
                accountId,
                IDENTITY_PROVIDER
        );
        outboxPublisher.publish(
                AccountProvisioningService.OUTBOX_AGGREGATE_TYPE,
                accountId.value().toString(),
                AccountProvisioningService.OUTBOX_EVENT_TYPE,
                null
        );
    }

    private static boolean isPending(Account account) {
        return account != null && account.getStatus() == AccountStatus.PENDING;
    }

    private static boolean isCurrent(AccountDirectoryProvisioningState state) {
        return state != null
                && state.getStatus() == AccountDirectoryProvisioningStatus.SYNCED
                && state.getSyncedRevision() == state.getDesiredRevision()
                && state.getLastSyncedAt() != null;
    }

    private static boolean isCurrent(AccountProvisioningState state) {
        return state != null
                && state.getStatus() == AccountProvisioningStatus.SYNCED
                && state.getSyncedRevision() == state.getDesiredRevision()
                && state.getLastSyncedAt() != null;
    }

    private static boolean providerNeedsRefreshAfterDirectory(
            AccountDirectoryProvisioningState directoryState,
            AccountProvisioningState providerState
    ) {
        Instant directorySyncedAt = directoryState.getLastSyncedAt();
        Instant providerSyncedAt = providerState.getLastSyncedAt();
        return directorySyncedAt != null
                && (providerSyncedAt == null || providerSyncedAt.isBefore(directorySyncedAt));
    }

    private static boolean hasExternalBinding(
            AccountProvisioningState providerState
    ) {
        return providerState != null
                && providerState.getExternalId() != null
                && !providerState.getExternalId().isBlank();
    }
}