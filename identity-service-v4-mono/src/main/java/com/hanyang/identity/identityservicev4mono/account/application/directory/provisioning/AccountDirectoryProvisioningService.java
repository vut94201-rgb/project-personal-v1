package com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning;


import com.hanyang.identity.identityservicev4mono.account.application.activation.AccountActivationCoordinator;
import com.hanyang.identity.identityservicev4mono.account.application.port.AccountDirectoryPort;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfile;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfileRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;


@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "integration.ds389",
        name = "enabled",
        havingValue = "true"
)
public class AccountDirectoryProvisioningService {

    public static final String OUTBOX_AGGREGATE_TYPE = "ACCOUNT";
    public static final String OUTBOX_EVENT_TYPE = "ACCOUNT_DIRECTORY_PROVISIONING_REQUESTED";

    private static final DirectoryProviderType PROVIDER = DirectoryProviderType.DS389;

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final AccountDirectoryProvisioningStateRepository provisioningStateRepository;
    private final AccountDirectoryPort accountDirectoryPort;
    private final AccountActivationCoordinator activationCoordinator;
    private final OutboxPublisher outboxPublisher;
    private final Clock clock;

    @Transactional
    public void requestSynchronization(AccountId accountId) {
        provisioningStateRepository.requestSynchronization(accountId, PROVIDER);
        outboxPublisher.publish(
                OUTBOX_AGGREGATE_TYPE,
                accountId.value().toString(),
                OUTBOX_EVENT_TYPE,
                null
        );
    }

    public AccountDirectoryReconciliationResult reconcile(AccountId accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return AccountDirectoryReconciliationResult.failed(
                    accountId,
                    PROVIDER,
                    "Account not found: " + accountId.value()
            );
        }

        AccountDirectoryProvisioningState syncingState = provisioningStateRepository
                .beginSynchronization(accountId, PROVIDER);
        long synchronizedRevision = syncingState.getDesiredRevision();

        AccountDirectoryProvisioningState synchronizedState;
        try {
            AccountStatus attemptedStatus = account.getStatus();
            AccountDirectoryPort.DirectoryAccount directoryAccount =
                    accountDirectoryPort.ensureAccount(directorySpec(account));

            Account latestAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Account disappeared during directory reconciliation: "
                                    + accountId.value()
                    ));

            if (authenticationAllowed(attemptedStatus)
                    != authenticationAllowed(latestAccount.getStatus())) {
                directoryAccount = accountDirectoryPort.setAuthenticationAllowed(
                        latestAccount.getUsername(),
                        authenticationAllowed(latestAccount.getStatus())
                );
            }

            Instant synchronizedAt = clock.instant();
            synchronizedState = provisioningStateRepository.completeSynchronization(
                    accountId,
                    PROVIDER,
                    synchronizedRevision,
                    directoryAccount.externalDn(),
                    directoryAccount.username(),
                    synchronizedAt
            );
        } catch (RuntimeException exception) {
            AccountDirectoryProvisioningState failedState =
                    provisioningStateRepository.failSynchronization(
                            accountId,
                            PROVIDER,
                            synchronizedRevision,
                            messageOf(exception)
                    );

            return AccountDirectoryReconciliationResult.fromState(failedState);
        }

        // Directory is the prerequisite for a federated Keycloak user. The
        // LDAP state is already durably SYNCED before coordination starts.
        activationCoordinator.afterDirectorySynchronization(accountId);
        return AccountDirectoryReconciliationResult.fromState(synchronizedState);
    }

    private AccountDirectoryPort.DirectoryAccountSpec directorySpec(Account account) {
        Employee employee = employeeRepository.findById(account.getEmployeeId())
                .orElseThrow(() -> new IllegalStateException(
                        "Employee not found for account "
                                + account.getId().value()
                                + ": "
                                + account.getEmployeeId().value()
                ));

        String email = employeeProfileRepository
                .findByEmployeeId(account.getEmployeeId())
                .map(EmployeeProfile::getEmail)
                .orElse(null);

        /*
         * inetOrgPerson requires sn. Employee currently stores one normalized
         * fullName instead of structured given/surname fields, so use fullName
         * for sn as a lossless technical fallback. Do not guess name parts.
         */
        return new AccountDirectoryPort.DirectoryAccountSpec(
                account.getUsername(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getFullName(),
                email,
                authenticationAllowed(account.getStatus())
        );
    }

    private static boolean authenticationAllowed(AccountStatus status) {
        return status == AccountStatus.ACTIVE;
    }

    private static String messageOf(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}