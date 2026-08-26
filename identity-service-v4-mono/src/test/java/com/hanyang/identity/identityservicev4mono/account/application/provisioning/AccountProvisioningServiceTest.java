package com.hanyang.identity.identityservicev4mono.account.application.provisioning;


import com.hanyang.identity.identityservicev4mono.account.application.activation.AccountActivationCoordinator;
import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderAccountPort;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;



class AccountProvisioningServiceTest {

    @Test
    void pendingAccountIsLinkedButRemainsDisabledAndPending() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountProvisioningStateRepository stateRepository =
                mock(AccountProvisioningStateRepository.class);
        IdentityProviderAccountPort providerPort = mock(IdentityProviderAccountPort.class);
        AccountActivationCoordinator activationCoordinator = mock(AccountActivationCoordinator.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-26T00:00:00Z"),
                ZoneOffset.UTC
        );

        when(accountRepository.findById(account.getId()))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountProvisioningState state = AccountProvisioningState.pending(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        );
        state.beginSynchronization();
        when(stateRepository.beginSynchronization(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        )).thenReturn(state);
        when(stateRepository.completeSynchronization(
                eq(account.getId()),
                eq(IdentityProviderType.KEYCLOAK),
                eq(state.getDesiredRevision()),
                any(),
                any(),
                any()
        )).thenAnswer(invocation -> {
            state.markSynchronized(
                    invocation.getArgument(2),
                    invocation.getArgument(3),
                    invocation.getArgument(4),
                    invocation.getArgument(5)
            );
            return state;
        });

        IdentityProviderAccountPort.ProvisionedAccount provisioned =
                new IdentityProviderAccountPort.ProvisionedAccount(
                        "kc-user-001",
                        "emp001"
                );

        when(providerPort.ensureAccount("emp001", null, false))
                .thenReturn(provisioned);

        AccountProvisioningService service = new AccountProvisioningService(
                accountRepository,
                stateRepository,
                providerPort,
                activationCoordinator,
                outboxPublisher,
                clock
        );

        AccountReconciliationResult result = service.reconcile(account.getId());

        assertEquals(AccountProvisioningStatus.SYNCED, result.status());
        assertEquals(AccountStatus.PENDING, account.getStatus());
        assertEquals("kc-user-001", account.getKeycloakSubject());

        verify(providerPort).ensureAccount("emp001", null, false);
        verify(providerPort, never()).ensureAccount(anyString(), any(), eq(true));
        verify(providerPort, never()).disableAccount(anyString(), any());
        verify(activationCoordinator).afterIdentityProviderSynchronization(account.getId());
    }

    @Test
    void disabledAccountIsNeverEnabledDuringReconciliation() {
        Account account = Account.rehydrate(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001",
                "kc-user-001",
                AccountStatus.DISABLED
        );
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountProvisioningStateRepository stateRepository =
                mock(AccountProvisioningStateRepository.class);
        IdentityProviderAccountPort providerPort = mock(IdentityProviderAccountPort.class);
        AccountActivationCoordinator activationCoordinator = mock(AccountActivationCoordinator.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);
        Clock clock = Clock.systemUTC();

        when(accountRepository.findById(account.getId()))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountProvisioningState state = AccountProvisioningState.pending(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        );
        state.beginSynchronization();
        when(stateRepository.beginSynchronization(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        )).thenReturn(state);
        when(stateRepository.completeSynchronization(
                eq(account.getId()),
                eq(IdentityProviderType.KEYCLOAK),
                eq(state.getDesiredRevision()),
                any(),
                any(),
                any()
        )).thenAnswer(invocation -> {
            state.markSynchronized(
                    invocation.getArgument(2),
                    invocation.getArgument(3),
                    invocation.getArgument(4),
                    invocation.getArgument(5)
            );
            return state;
        });

        IdentityProviderAccountPort.ProvisionedAccount disabled =
                new IdentityProviderAccountPort.ProvisionedAccount(
                        "kc-user-001",
                        "emp001"
                );
        when(providerPort.disableAccount("emp001", "kc-user-001"))
                .thenReturn(disabled);

        AccountProvisioningService service = new AccountProvisioningService(
                accountRepository,
                stateRepository,
                providerPort,
                activationCoordinator,
                outboxPublisher,
                clock
        );

        AccountReconciliationResult result = service.reconcile(account.getId());

        assertEquals(AccountProvisioningStatus.SYNCED, result.status());
        assertEquals(AccountStatus.DISABLED, account.getStatus());
        verify(providerPort).disableAccount("emp001", "kc-user-001");
        verify(providerPort, never()).ensureAccount(anyString(), any(), eq(true));
        verify(activationCoordinator).afterIdentityProviderSynchronization(account.getId());
    }
}