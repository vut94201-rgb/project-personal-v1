package com.hanyang.identity.identityservicev4mono.account.application;

import com.hanyang.identity.identityservicev4mono.account.application.command.CreateAccountCommand;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountNotFoundException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountProvisioningNotAllowedException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.EmployeeAlreadyHasAccountException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.UsernameAlreadyExistsException;

import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountReconciliationResult;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@IdentityAdminAccess
@Service
@RequiredArgsConstructor
public class AccountCommandService {

  private final AccountRepository accountRepository;
  private final EmployeeRepository employeeRepository;
  private final AccountProvisioningService provisioningService;
  private final ObjectProvider<AccountDirectoryProvisioningService> directoryProvisioningServiceProvider;

  @Transactional
  public Account create(CreateAccountCommand command) {

    employeeRepository
            .findById(command.employeeId())
            .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

    if (accountRepository.existsByEmployeeId(command.employeeId())) {

      throw new EmployeeAlreadyHasAccountException(command.employeeId());
    }

    if (accountRepository.existsByUsername(command.username())) {

      throw new UsernameAlreadyExistsException(command.username());
    }

    Account account = Account.create(AccountId.newId(), command.employeeId(), command.username());

    Account saved = accountRepository.save(account);

    // New accounts enter the directory first and remain PENDING/locked.
    // Keycloak account resolution is intentionally not the normal create
    // path. The activation coordinator schedules Keycloak only after the
    // directory identity is current, then activates when both targets sync.
    AccountDirectoryProvisioningService directoryProvisioningService =
            directoryProvisioningServiceProvider.getIfAvailable();
    if (directoryProvisioningService != null) {
      directoryProvisioningService.requestSynchronization(saved.getId());
    }

    return saved;
  }

  /**
   * Legacy/manual Keycloak preparation entry point kept for the existing REST API.
   *
   * <p>It may create or repair the Keycloak provisioning binding. Activation
   * remains coordinator-owned: a PENDING account becomes ACTIVE only when the
   * directory and Keycloak provisioning states are both current.</p>
   */
  @Transactional
  public Account provision(AccountId accountId) {
    Account account =
            accountRepository
                    .findById(accountId)
                    .orElseThrow(() -> new AccountNotFoundException(accountId));

    if (account.getStatus() != AccountStatus.PENDING) {
      throw new AccountProvisioningNotAllowedException(accountId, account.getStatus());
    }

    AccountReconciliationResult result = provisioningService.reconcile(accountId);
    if (result.status() != AccountProvisioningStatus.SYNCED) {
      throw new IllegalStateException(
              result.error() == null || result.error().isBlank()
                      ? "Account provisioning did not complete successfully. status="
                      + result.status()
                      + ", accountId="
                      + accountId.value()
                      : result.error());
    }

    return accountRepository
            .findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
  }

  @Transactional
  public void disable(AccountId accountId) {

    Account account =
            accountRepository
                    .findById(accountId)
                    .orElseThrow(() -> new AccountNotFoundException(accountId));

    account.disable();
    accountRepository.save(account);

    AccountDirectoryProvisioningService directoryProvisioningService =
            directoryProvisioningServiceProvider.getIfAvailable();
    if (directoryProvisioningService != null) {
      directoryProvisioningService.requestSynchronization(accountId);
    }

    provisioningService.requestSynchronization(accountId);
  }
}