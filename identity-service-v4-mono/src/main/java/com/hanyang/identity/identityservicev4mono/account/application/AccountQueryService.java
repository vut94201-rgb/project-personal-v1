package com.hanyang.identity.identityservicev4mono.account.application;

import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountNotFoundException;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

@Service
@IdentityReadAccess
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountQueryService {

  private final AccountRepository accountRepository;

  public Account getById(AccountId id) {
    return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
  }

  public Account findAccountByEmployeeIdOrUsernameOrEmployeeCode(
      @Nullable UUID employeeId, @Nullable String username, @Nullable String employeeCode) {
    return accountRepository
        .findAccountByEmployeeIdOrUsernameOrEmployeeCode(employeeId, username, employeeCode)
        .orElseThrow(
            () ->
                new AccountNotFoundException(
                    resolveAccountIdentifier(employeeId, username, employeeCode)));
  }

  private String resolveAccountIdentifier(
      @Nullable UUID employeeId, @Nullable String username, @Nullable String employeeCode) {
    if (Objects.isNull(employeeId)) {
      return username;
    }
    if (StringUtils.hasText(employeeCode)) {
      return employeeCode;
    }
    if (StringUtils.hasText(username)) {
      return username;
    }
    return "no search criteria";
  }
}
