package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;

public class AccountRoleNotAssignedException extends RuntimeException {

    public AccountRoleNotAssignedException(
            AccountId accountId,
            RoleId roleId
    ) {
        super(
                "Role " + roleId.value()
                        + " is not assigned to account "
                        + accountId.value()
        );
    }
}