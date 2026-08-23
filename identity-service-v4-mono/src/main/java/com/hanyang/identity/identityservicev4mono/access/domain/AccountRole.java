package com.hanyang.identity.identityservicev4mono.access.domain;


import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import lombok.Getter;

import java.util.Objects;

@Getter
public class AccountRole {

    private final AccountId accountId;
    private final RoleId roleId;

    private AccountRole(
            AccountId accountId,
            RoleId roleId
    ) {
        this.accountId = Objects.requireNonNull(
                accountId,
                "accountId must not be null"
        );
        this.roleId = Objects.requireNonNull(
                roleId,
                "roleId must not be null"
        );
    }

    public static AccountRole create(
            AccountId accountId,
            RoleId roleId
    ) {
        return new AccountRole(accountId, roleId);
    }

    public static AccountRole rehydrate(
            AccountId accountId,
            RoleId roleId
    ) {
        return new AccountRole(accountId, roleId);
    }
}