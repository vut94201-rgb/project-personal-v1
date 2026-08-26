package com.hanyang.identity.identityservicev4mono.account.application.exception;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;

public class AccountCredentialEmailUnavailableException
        extends RuntimeException {

    public AccountCredentialEmailUnavailableException(AccountId accountId) {
        super(
                "Password setup email cannot be sent because the provisioned identity has no email address. accountId="
                        + accountId.value()
        );
    }
}