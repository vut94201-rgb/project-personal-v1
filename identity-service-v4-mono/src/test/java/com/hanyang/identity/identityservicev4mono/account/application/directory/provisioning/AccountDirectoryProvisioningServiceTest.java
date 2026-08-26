package com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning;


import com.hanyang.identity.identityservicev4mono.account.application.activation.AccountActivationCoordinator;
import com.hanyang.identity.identityservicev4mono.account.application.port.AccountDirectoryPort;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfileRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AccountDirectoryProvisioningServiceTest {

    @Test
    void pendingAccountIsProvisionedLockedAndRemainsPending() {
        EmployeeId employeeId = EmployeeId.newId();
        Account account = Account.create(
                AccountId.newId(),
                employeeId,
                "emp001"
        );
        Employee employee = Employee.create(
                employeeId,
                "E000001",
                "Test User"
        );

        AccountRepository accountRepository = mock(AccountRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeProfileRepository profileRepository = mock(EmployeeProfileRepository.class);
        AccountDirectoryProvisioningStateRepository stateRepository =
                mock(AccountDirectoryProvisioningStateRepository.class);
        AccountDirectoryPort directoryPort = mock(AccountDirectoryPort.class);
        AccountActivationCoordinator activationCoordinator = mock(AccountActivationCoordinator.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-26T00:00:00Z"),
                ZoneOffset.UTC
        );

        when(accountRepository.findById(account.getId()))
                .thenReturn(Optional.of(account));
        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));
        when(profileRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.empty());

        AccountDirectoryProvisioningState state =
                AccountDirectoryProvisioningState.pending(
                        account.getId(),
                        DirectoryProviderType.DS389
                );
        state.beginSynchronization();

        when(stateRepository.beginSynchronization(
                account.getId(),
                DirectoryProviderType.DS389
        )).thenReturn(state);

        when(stateRepository.completeSynchronization(
                eq(account.getId()),
                eq(DirectoryProviderType.DS389),
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

        when(directoryPort.ensureAccount(any()))
                .thenReturn(new AccountDirectoryPort.DirectoryAccount(
                        "emp001",
                        "uid=emp001,ou=People,dc=hanyang,dc=local",
                        false
                ));

        AccountDirectoryProvisioningService service =
                new AccountDirectoryProvisioningService(
                        accountRepository,
                        employeeRepository,
                        profileRepository,
                        stateRepository,
                        directoryPort,
                        activationCoordinator,
                        outboxPublisher,
                        clock
                );

        AccountDirectoryReconciliationResult result =
                service.reconcile(account.getId());

        assertEquals(AccountDirectoryProvisioningStatus.SYNCED, result.status());
        assertEquals(AccountStatus.PENDING, account.getStatus());
        assertEquals(
                "uid=emp001,ou=People,dc=hanyang,dc=local",
                result.externalDn()
        );

        ArgumentCaptor<AccountDirectoryPort.DirectoryAccountSpec> specCaptor =
                ArgumentCaptor.forClass(AccountDirectoryPort.DirectoryAccountSpec.class);
        verify(directoryPort).ensureAccount(specCaptor.capture());

        AccountDirectoryPort.DirectoryAccountSpec spec = specCaptor.getValue();
        assertEquals("emp001", spec.username());
        assertEquals("E000001", spec.employeeNumber());
        assertEquals("Test User", spec.commonName());
        assertEquals("Test User", spec.surname());
        assertEquals(false, spec.authenticationAllowed());

        verify(directoryPort, never()).setAuthenticationAllowed(anyString(), eq(true));
        verify(activationCoordinator).afterDirectorySynchronization(account.getId());
    }

    @Test
    void statusChangeDuringRemoteCallIsCompensated() {
        EmployeeId employeeId = EmployeeId.newId();
        Account active = Account.rehydrate(
                AccountId.newId(),
                employeeId,
                "emp001",
                "kc-user-001",
                AccountStatus.ACTIVE
        );
        Account disabled = Account.rehydrate(
                active.getId(),
                employeeId,
                "emp001",
                "kc-user-001",
                AccountStatus.DISABLED
        );
        Employee employee = Employee.create(employeeId, "E000001", "Test User");

        AccountRepository accountRepository = mock(AccountRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeProfileRepository profileRepository = mock(EmployeeProfileRepository.class);
        AccountDirectoryProvisioningStateRepository stateRepository =
                mock(AccountDirectoryProvisioningStateRepository.class);
        AccountDirectoryPort directoryPort = mock(AccountDirectoryPort.class);
        AccountActivationCoordinator activationCoordinator = mock(AccountActivationCoordinator.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);
        Clock clock = Clock.systemUTC();

        when(accountRepository.findById(active.getId()))
                .thenReturn(Optional.of(active), Optional.of(disabled));
        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));
        when(profileRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.empty());

        AccountDirectoryProvisioningState state =
                AccountDirectoryProvisioningState.pending(
                        active.getId(),
                        DirectoryProviderType.DS389
                );
        state.beginSynchronization();

        when(stateRepository.beginSynchronization(
                active.getId(),
                DirectoryProviderType.DS389
        )).thenReturn(state);
        when(stateRepository.completeSynchronization(
                eq(active.getId()),
                eq(DirectoryProviderType.DS389),
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

        when(directoryPort.ensureAccount(any()))
                .thenReturn(new AccountDirectoryPort.DirectoryAccount(
                        "emp001",
                        "uid=emp001,ou=People,dc=hanyang,dc=local",
                        true
                ));
        when(directoryPort.setAuthenticationAllowed("emp001", false))
                .thenReturn(new AccountDirectoryPort.DirectoryAccount(
                        "emp001",
                        "uid=emp001,ou=People,dc=hanyang,dc=local",
                        false
                ));

        AccountDirectoryProvisioningService service =
                new AccountDirectoryProvisioningService(
                        accountRepository,
                        employeeRepository,
                        profileRepository,
                        stateRepository,
                        directoryPort,
                        activationCoordinator,
                        outboxPublisher,
                        clock
                );

        AccountDirectoryReconciliationResult result =
                service.reconcile(active.getId());

        assertEquals(AccountDirectoryProvisioningStatus.SYNCED, result.status());
        verify(directoryPort).setAuthenticationAllowed("emp001", false);
        verify(activationCoordinator).afterDirectorySynchronization(active.getId());
    }
}