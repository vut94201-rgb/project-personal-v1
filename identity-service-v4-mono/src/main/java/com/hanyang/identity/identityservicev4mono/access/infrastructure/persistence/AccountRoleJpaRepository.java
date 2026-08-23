package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRoleJpaRepository
        extends BaseJpaRepository<AccountRoleJpaEntity, AccountRoleJpaId> {

    List<AccountRoleJpaEntity> findAllById_AccountId(UUID accountId);
}