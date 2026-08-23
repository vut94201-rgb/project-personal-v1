package com.hanyang.identity.identityservicev4mono.account.application.exception;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;

public class AccountProvisioningNotAllowedException extends RuntimeException {
    public AccountProvisioningNotAllowedException(
            AccountId accountId,
            AccountStatus currentStatus
    ) {
        super(
                "Account %s cannot be provisioned while status is %s"
                        .formatted(accountId.value(), currentStatus)
        );
    }
}