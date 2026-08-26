package com.hanyang.identity.identityservicev4mono.account.application.activation;


import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class AccountActivationCoordinatorTest {

    @Test
    void directorySyncSchedulesFirstKeycloakReconciliationButDoesNotActivateYet() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );
        AccountDirectoryProvisioningState directoryState = synchronizedDirectoryState(
                account.getId(),
                Instant.parse("2026-08-26T01:00:00Z")
        );

        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountDirectoryProvisioningStateRepository directoryRepository =
                mock(AccountDirectoryProvisioningStateRepository.class);
        AccountProvisioningStateRepository providerRepository =
                mock(AccountProvisioningStateRepository.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(directoryRepository.findByAccountIdAndProvider(
                account.getId(),
                DirectoryProviderType.DS389
        )).thenReturn(Optional.of(directoryState));
        when(providerRepository.findByAccountIdAndProvider(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.empty());

        AccountActivationCoordinator coordinator = new AccountActivationCoordinator(
                accountRepository,
                directoryRepository,
                providerRepository,
                outboxPublisher
        );

        coordinator.afterDirectorySynchronization(account.getId());

        assertEquals(AccountStatus.PENDING, account.getStatus());
        verify(providerRepository).requestSynchronization(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        );
        verify(outboxPublisher).publish(
                AccountProvisioningService.OUTBOX_AGGREGATE_TYPE,
                account.getId().value().toString(),
                AccountProvisioningService.OUTBOX_EVENT_TYPE,
                null
        );
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void currentDirectoryAndKeycloakStatesActivateAndScheduleEnforcement() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );
        AccountDirectoryProvisioningState directoryState = synchronizedDirectoryState(
                account.getId(),
                Instant.parse("2026-08-26T01:00:00Z")
        );
        AccountProvisioningState providerState = synchronizedProviderState(
                account.getId(),
                "kc-fed-001",
                Instant.parse("2026-08-26T01:00:01Z")
        );

        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountDirectoryProvisioningStateRepository directoryRepository =
                mock(AccountDirectoryProvisioningStateRepository.class);
        AccountProvisioningStateRepository providerRepository =
                mock(AccountProvisioningStateRepository.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(directoryRepository.findByAccountIdAndProvider(
                account.getId(),
                DirectoryProviderType.DS389
        )).thenReturn(Optional.of(directoryState));
        when(providerRepository.findByAccountIdAndProvider(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(providerState));

        AccountActivationCoordinator coordinator = new AccountActivationCoordinator(
                accountRepository,
                directoryRepository,
                providerRepository,
                outboxPublisher
        );

        coordinator.afterIdentityProviderSynchronization(account.getId());

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        verify(accountRepository).save(account);
        verify(directoryRepository).requestSynchronization(
                account.getId(),
                DirectoryProviderType.DS389
        );
        verify(providerRepository).requestSynchronization(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        );
        verify(outboxPublisher).publish(
                AccountDirectoryProvisioningService.OUTBOX_AGGREGATE_TYPE,
                account.getId().value().toString(),
                AccountDirectoryProvisioningService.OUTBOX_EVENT_TYPE,
                null
        );
        verify(outboxPublisher).publish(
                AccountProvisioningService.OUTBOX_AGGREGATE_TYPE,
                account.getId().value().toString(),
                AccountProvisioningService.OUTBOX_EVENT_TYPE,
                null
        );
    }

    @Test
    void staleKeycloakStateIsRefreshedInsteadOfActivating() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );
        AccountDirectoryProvisioningState directoryState = synchronizedDirectoryState(
                account.getId(),
                Instant.parse("2026-08-26T02:00:00Z")
        );
        AccountProvisioningState providerState = synchronizedProviderState(
                account.getId(),
                "kc-fed-001",
                Instant.parse("2026-08-26T01:00:00Z")
        );

        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountDirectoryProvisioningStateRepository directoryRepository =
                mock(AccountDirectoryProvisioningStateRepository.class);
        AccountProvisioningStateRepository providerRepository =
                mock(AccountProvisioningStateRepository.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(directoryRepository.findByAccountIdAndProvider(
                account.getId(), DirectoryProviderType.DS389
        )).thenReturn(Optional.of(directoryState));
        when(providerRepository.findByAccountIdAndProvider(
                account.getId(), IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(providerState));

        AccountActivationCoordinator coordinator = new AccountActivationCoordinator(
                accountRepository,
                directoryRepository,
                providerRepository,
                outboxPublisher
        );

        coordinator.afterDirectorySynchronization(account.getId());

        assertEquals(AccountStatus.PENDING, account.getStatus());
        verify(providerRepository).requestSynchronization(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        );
        verify(accountRepository, never()).save(any(Account.class));
    }


    @Test
    void freshDirectorySyncRetriesAPreviouslyFailedKeycloakAttempt() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );
        AccountDirectoryProvisioningState directoryState = synchronizedDirectoryState(
                account.getId(),
                Instant.parse("2026-08-26T03:00:00Z")
        );
        AccountProvisioningState failedProviderState = AccountProvisioningState.pending(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        );
        long attemptedRevision = failedProviderState.beginSynchronization();
        failedProviderState.markFailed(
                attemptedRevision,
                "Federated Keycloak user not found"
        );

        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountDirectoryProvisioningStateRepository directoryRepository =
                mock(AccountDirectoryProvisioningStateRepository.class);
        AccountProvisioningStateRepository providerRepository =
                mock(AccountProvisioningStateRepository.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(directoryRepository.findByAccountIdAndProvider(
                account.getId(), DirectoryProviderType.DS389
        )).thenReturn(Optional.of(directoryState));
        when(providerRepository.findByAccountIdAndProvider(
                account.getId(), IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(failedProviderState));

        AccountActivationCoordinator coordinator = new AccountActivationCoordinator(
                accountRepository,
                directoryRepository,
                providerRepository,
                outboxPublisher
        );

        coordinator.afterDirectorySynchronization(account.getId());

        assertEquals(AccountStatus.PENDING, account.getStatus());
        verify(providerRepository).requestSynchronization(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        );
        verify(accountRepository, never()).save(any(Account.class));
    }


    @Test
    void syncedProviderStateWithoutExternalIdDoesNotActivate() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );
        AccountDirectoryProvisioningState directoryState = synchronizedDirectoryState(
                account.getId(),
                Instant.parse("2026-08-26T05:00:00Z")
        );
        AccountProvisioningState providerState = synchronizedProviderState(
                account.getId(),
                null,
                Instant.parse("2026-08-26T05:00:01Z")
        );

        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountDirectoryProvisioningStateRepository directoryRepository =
                mock(AccountDirectoryProvisioningStateRepository.class);
        AccountProvisioningStateRepository providerRepository =
                mock(AccountProvisioningStateRepository.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(directoryRepository.findByAccountIdAndProvider(
                account.getId(), DirectoryProviderType.DS389
        )).thenReturn(Optional.of(directoryState));
        when(providerRepository.findByAccountIdAndProvider(
                account.getId(), IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(providerState));

        AccountActivationCoordinator coordinator = new AccountActivationCoordinator(
                accountRepository,
                directoryRepository,
                providerRepository,
                outboxPublisher
        );

        assertThrows(
                IllegalStateException.class,
                () -> coordinator.afterIdentityProviderSynchronization(account.getId())
        );
        assertEquals(AccountStatus.PENDING, account.getStatus());
        verify(accountRepository, never()).save(any(Account.class));
    }

    private static AccountDirectoryProvisioningState synchronizedDirectoryState(
            AccountId accountId,
            Instant synchronizedAt
    ) {
        AccountDirectoryProvisioningState state = AccountDirectoryProvisioningState.pending(
                accountId,
                DirectoryProviderType.DS389
        );
        long revision = state.beginSynchronization();
        state.markSynchronized(
                revision,
                "uid=emp001,ou=People,dc=hanyang,dc=local",
                "emp001",
                synchronizedAt
        );
        return state;
    }

    private static AccountProvisioningState synchronizedProviderState(
            AccountId accountId,
            String externalId,
            Instant synchronizedAt
    ) {
        AccountProvisioningState state = AccountProvisioningState.pending(
                accountId,
                IdentityProviderType.KEYCLOAK
        );
        long revision = state.beginSynchronization();
        state.markSynchronized(
                revision,
                externalId,
                "emp001",
                synchronizedAt
        );
        return state;
    }
}