package com.hanyang.identity.identityservicev4mono.account.application.exception;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;

public class AccountCredentialOnboardingNotAllowedException
        extends RuntimeException {

    public AccountCredentialOnboardingNotAllowedException(
            AccountId accountId,
            AccountStatus status
    ) {
        super(
                "Credential onboarding requires a provisioned ACTIVE account. accountId="
                        + accountId.value()
                        + ", status="
                        + status
        );
    }
}