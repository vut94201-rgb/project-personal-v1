package com.hanyang.identity.identityservicev4mono.access.application.exception;


import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;

public class AccountRoleAlreadyAssignedException extends RuntimeException {

    public AccountRoleAlreadyAssignedException(
            AccountId accountId,
            RoleId roleId
    ) {
        super(
                "Role " + roleId.value()
                        + " is already assigned to account "
                        + accountId.value()
        );
    }
}