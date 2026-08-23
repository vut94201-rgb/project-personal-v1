package com.hanyang.identity.identityservicev4mono.account.application;

import com.hanyang.identity.identityservicev4mono.account.application.command.CreateAccountCommand;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountNotFoundException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountProvisioningNotAllowedException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.EmployeeAlreadyHasAccountException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.UsernameAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderAccountPort;
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
    private final IdentityProviderAccountPort identityProviderAccountPort;

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

        return accountRepository.save(account);
    }

    @Transactional
    public Account provision(AccountId accountId) {

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(accountId)
                        );

        if (account.getStatus() != AccountStatus.PENDING) {
            throw new AccountProvisioningNotAllowedException(
                    accountId,
                    account.getStatus()
            );
        }

        String keycloakSubject =
                identityProviderAccountPort.createUser(
                        account.getUsername()
                );

        account.provision(keycloakSubject);

        return accountRepository.save(account);
    }

    @Transactional
    public void disable(AccountId accountId) {

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(accountId)
                        );

        if (account.getKeycloakSubject() != null
                && !account.getKeycloakSubject().isBlank()) {
            identityProviderAccountPort.disableUser(
                    account.getKeycloakSubject()
            );
        }

        account.disable();

        accountRepository.save(account);
    }
}