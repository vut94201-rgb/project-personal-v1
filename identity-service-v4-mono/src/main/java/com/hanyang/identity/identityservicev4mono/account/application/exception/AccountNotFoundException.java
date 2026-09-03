package com.hanyang.identity.identityservicev4mono.account.application.exception;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(AccountId id) {
    super("Account not found: " + id.value());
  }

  public AccountNotFoundException(UUID id) {
    super("Account not found: " + id.toString());
  }
    public AccountNotFoundException(String message) {
        super("Account not found: " + message);
    }
}
