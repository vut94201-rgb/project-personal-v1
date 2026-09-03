package com.hanyang.identity.identityservicev4mono.account.application;
import com.hanyang.identity.identityservicev4mono.account.application.command.CreateAccountCommand;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountCreationDirectoryProvisioningTest {

    @Test
    void createSchedulesDirectorySynchronizationButDoesNotScheduleKeycloakDirectly() {
        EmployeeId employeeId = EmployeeId.newId();
        Employee employee = Employee.create(employeeId, "HY000042", "Nguyen Van A");

        AccountRepository accountRepository = mock(AccountRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AccountProvisioningService keycloakProvisioningService =
                mock(AccountProvisioningService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AccountDirectoryProvisioningService> directoryProvider =
                mock(ObjectProvider.class);
        AccountDirectoryProvisioningService directoryProvisioningService =
                mock(AccountDirectoryProvisioningService.class);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(accountRepository.existsByEmployeeId(employeeId)).thenReturn(false);
        when(accountRepository.existsByUsername("nguyenvana")).thenReturn(false);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(directoryProvider.getIfAvailable()).thenReturn(directoryProvisioningService);

        AccountCommandService service = new AccountCommandService(
                accountRepository,
                employeeRepository,
                keycloakProvisioningService,
                directoryProvider
        );

        Account account = service.create(
                new CreateAccountCommand(employeeId, "nguyenvana")
        );

        assertEquals(AccountStatus.PENDING, account.getStatus());
        verify(directoryProvisioningService).requestSynchronization(account.getId());
        verify(keycloakProvisioningService, never()).requestSynchronization(any());
    }
}