package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.exception.*;
import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderAccessPort;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.AccountRoleProvisioningService;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountNotFoundException;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@IdentityAdminAccess
@Service
@RequiredArgsConstructor
public class AccountRoleCommandService {

  private final AccountRepository accountRepository;
  private final RoleRepository roleRepository;
  private final ApplicationRepository applicationRepository;
  private final AccountRoleRepository accountRoleRepository;
  private final AccountRoleProvisioningService provisioningService;

  @Transactional
  public void assign(AccountId accountId, RoleId roleId) {
    Account account = getAccount(accountId);
    Role role = getRole(roleId);

      validateAssignable(account, role);


    if (accountRoleRepository.exists(accountId, roleId)) {
      throw new AccountRoleAlreadyAssignedException(accountId, roleId);
    }

    accountRoleRepository.save(AccountRole.create(accountId, roleId));

    provisioningService.requestSynchronization(accountId, roleId, true);
  }

  @Transactional
  public void revoke(AccountId accountId, RoleId roleId) {
    getAccount(accountId);
    getRole(roleId);

    if (!accountRoleRepository.exists(accountId, roleId)) {
      throw new AccountRoleNotAssignedException(accountId, roleId);
    }

    accountRoleRepository.delete(accountId, roleId);
    provisioningService.requestSynchronization(accountId, roleId, false);
  }

  private Account getAccount(AccountId accountId) {
    return accountRepository
        .findById(accountId)
        .orElseThrow(() -> new AccountNotFoundException(accountId));
  }

  private Role getRole(RoleId roleId) {
    return roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
  }

  private void validateAssignable(Account account, Role role) {
    if (account.getStatus() == AccountStatus.DISABLED) {
      throw new AccountDisabledException(account.getId());
    }

    if (account.getStatus() != AccountStatus.ACTIVE) {
      throw new AccountNotProvisionedException(account.getId());
    }

    if (role.getStatus() != RoleStatus.ACTIVE) {
      throw new RoleDisabledException(role.getId());
    }

    Application application =
        applicationRepository
            .findById(role.getApplicationId())
            .orElseThrow(() -> new ApplicationNotFoundException(role.getApplicationId()));

    if (application.getStatus() != ApplicationStatus.ACTIVE) {
      throw new ApplicationDisabledException(application.getId());
    }
  }
}
