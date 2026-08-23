package com.hanyang.identity.identityservicev4mono.account.application.exception;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;

public class AccountNotFoundException
        extends RuntimeException {

    public AccountNotFoundException(AccountId id) {
        super("Account not found: " + id.value());
    }
}