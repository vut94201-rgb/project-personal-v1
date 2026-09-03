package com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning;

import com.hanyang.identity.identityservicev4mono.account.application.activation.AccountActivationCoordinator;
import com.hanyang.identity.identityservicev4mono.account.application.port.AccountDirectoryPort;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfileRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AccountDirectoryProvisioningRequestTest {

    @Test
    void requestSynchronizationPersistsDesiredStateAndPublishesOutboxWithoutCallingDirectory() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeProfileRepository employeeProfileRepository =
                mock(EmployeeProfileRepository.class);
        AccountDirectoryProvisioningStateRepository stateRepository =
                mock(AccountDirectoryProvisioningStateRepository.class);
        AccountDirectoryPort directoryPort = mock(AccountDirectoryPort.class);
        AccountActivationCoordinator activationCoordinator =
                mock(AccountActivationCoordinator.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        AccountDirectoryProvisioningService service =
                new AccountDirectoryProvisioningService(
                        accountRepository,
                        employeeRepository,
                        employeeProfileRepository,
                        stateRepository,
                        directoryPort,
                        activationCoordinator,
                        outboxPublisher,
                        Clock.systemUTC()
                );

        AccountId accountId = AccountId.newId();

        service.requestSynchronization(accountId);

        verify(stateRepository).requestSynchronization(
                accountId,
                DirectoryProviderType.DS389
        );
        verify(outboxPublisher).publish(
                AccountDirectoryProvisioningService.OUTBOX_AGGREGATE_TYPE,
                accountId.value().toString(),
                AccountDirectoryProvisioningService.OUTBOX_EVENT_TYPE,
                null
        );
        verifyNoInteractions(directoryPort, activationCoordinator);
    }
}