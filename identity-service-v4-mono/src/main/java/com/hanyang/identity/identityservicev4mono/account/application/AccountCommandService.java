package com.hanyang.identity.identityservicev4mono.account.application;

import com.hanyang.identity.identityservicev4mono.account.application.command.CreateAccountCommand;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountNotFoundException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountProvisioningNotAllowedException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.EmployeeAlreadyHasAccountException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.UsernameAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderAccountPort;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountReconciliationResult;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AccountCommandService {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountProvisioningService provisioningService;

    @Transactional
    public Account create(CreateAccountCommand command) {

        employeeRepository.findById(command.employeeId())
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                command.employeeId()
                        )
                );

        if (accountRepository.existsByEmployeeId(
                command.employeeId())) {

            throw new EmployeeAlreadyHasAccountException(
                    command.employeeId()
            );
        }

        if (accountRepository.existsByUsername(
                command.username())) {

            throw new UsernameAlreadyExistsException(
                    command.username()
            );
        }

        Account account = Account.create(
                AccountId.newId(),
                command.employeeId(),
                command.username()
        );

        Account saved = accountRepository.save(account);
        provisioningService.requestSynchronization(saved.getId());
        return saved;
    }

    /**
     * Immediate/manual provisioning entry point kept for the existing REST API.
     * Normal delivery is now driven by the transactional outbox.
     */
    public Account provision(AccountId accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getStatus() != AccountStatus.PENDING) {
            throw new AccountProvisioningNotAllowedException(
                    accountId,
                    account.getStatus()
            );
        }

        AccountReconciliationResult result = provisioningService.reconcile(accountId);
        if (result.status() != AccountProvisioningStatus.SYNCED) {
            throw new IllegalStateException(
                    result.error() == null || result.error().isBlank()
                            ? "Account provisioning did not complete successfully. status="
                            + result.status()
                            + ", accountId="
                            + accountId.value()
                            : result.error()
            );
        }

        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Transactional
    public void disable(AccountId accountId) {

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(accountId)
                        );

        account.disable();
        accountRepository.save(account);
        provisioningService.requestSynchronization(accountId);
    }
}