package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;

public class AccountNotProvisionedException extends RuntimeException {

            public AccountNotProvisionedException(AccountId id) {
                super("Account must be active and provisioned before role assignment: " + id.value());
            }
}