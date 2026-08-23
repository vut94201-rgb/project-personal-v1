package com.hanyang.identity.identityservicev4mono.access.domain;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;

import java.util.List;

public interface AccountRoleRepository {

    AccountRole save(AccountRole accountRole);

    void delete(AccountId accountId, RoleId roleId);

    boolean exists(AccountId accountId, RoleId roleId);

    List<RoleId> findRoleIdsByAccountId(AccountId accountId);
}