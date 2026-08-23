package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException(AccountId id) {
        super("Account is disabled: " + id.value());
    }
}