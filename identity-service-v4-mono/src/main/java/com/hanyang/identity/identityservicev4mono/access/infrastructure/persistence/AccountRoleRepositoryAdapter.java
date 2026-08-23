package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.access.domain.AccountRole;
import com.hanyang.identity.identityservicev4mono.access.domain.AccountRoleRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccountRoleRepositoryAdapter
        implements AccountRoleRepository {

    private final AccountRoleJpaRepository jpaRepository;

    @Override
    public AccountRole save(AccountRole accountRole) {
        AccountRoleJpaId id = toJpaId(
                accountRole.getAccountId(),
                accountRole.getRoleId()
        );

        AccountRoleJpaEntity entity = new AccountRoleJpaEntity();
        entity.setId(id);

        AccountRoleJpaEntity saved = jpaRepository.save(entity);

        return AccountRole.rehydrate(
                new AccountId(saved.getId().getAccountId()),
                new RoleId(saved.getId().getRoleId())
        );
    }

    @Override
    public void delete(
            AccountId accountId,
            RoleId roleId
    ) {
        jpaRepository.deleteById(toJpaId(accountId, roleId));
    }

    @Override
    public boolean exists(
            AccountId accountId,
            RoleId roleId
    ) {
        return jpaRepository.existsById(
                toJpaId(accountId, roleId)
        );
    }

    @Override
    public List<RoleId> findRoleIdsByAccountId(AccountId accountId) {
        return jpaRepository
                .findAllById_AccountId(accountId.value())
                .stream()
                .map(entity ->
                        new RoleId(entity.getId().getRoleId())
                )
                .toList();
    }

    private AccountRoleJpaId toJpaId(
            AccountId accountId,
            RoleId roleId
    ) {
        return new AccountRoleJpaId(
                accountId.value(),
                roleId.value()
        );
    }
}