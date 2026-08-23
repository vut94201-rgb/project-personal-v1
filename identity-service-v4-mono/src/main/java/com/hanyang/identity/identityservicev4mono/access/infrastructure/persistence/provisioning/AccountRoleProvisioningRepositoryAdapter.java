package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.provisioning.AccountRoleProvisioningState;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.AccountRoleProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRoleProvisioningRepositoryAdapter
        implements AccountRoleProvisioningStateRepository {

    private final AccountRoleProvisioningJpaRepository jpaRepository;

    @Override
    @Transactional
    public AccountRoleProvisioningState requestSynchronization(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned
    ) {
        Optional<AccountRoleProvisioningJpaEntity> existing = jpaRepository
                .findByAccountIdAndRoleIdAndProvider(
                        accountId.value(),
                        roleId.value(),
                        provider
                );

        if (existing.isEmpty()) {
            return toDomain(jpaRepository.save(
                    newEntity(AccountRoleProvisioningState.pending(
                            accountId,
                            roleId,
                            provider,
                            desiredAssigned
                    ))
            ));
        }

        AccountRoleProvisioningJpaEntity entity = existing.get();
        AccountRoleProvisioningState state = toDomain(entity);
        state.requestSynchronization(desiredAssigned);
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public AccountRoleProvisioningState beginSynchronization(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider
    ) {
        AccountRoleProvisioningJpaEntity entity = jpaRepository
                .findByAccountIdAndRoleIdAndProvider(
                        accountId.value(),
                        roleId.value(),
                        provider
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Account-role provisioning state not found for account "
                                + accountId.value()
                                + ", role "
                                + roleId.value()
                                + " and provider "
                                + provider
                ));

        AccountRoleProvisioningState state = toDomain(entity);
        state.beginSynchronization();
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public AccountRoleProvisioningState completeSynchronization(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            long synchronizedRevision,
            Instant synchronizedAt
    ) {
        AccountRoleProvisioningJpaEntity entity = requiredEntity(
                accountId,
                roleId,
                provider
        );
        AccountRoleProvisioningState state = toDomain(entity);
        state.markSynchronized(synchronizedRevision, synchronizedAt);
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public AccountRoleProvisioningState failSynchronization(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    ) {
        AccountRoleProvisioningJpaEntity entity = requiredEntity(
                accountId,
                roleId,
                provider
        );
        AccountRoleProvisioningState state = toDomain(entity);
        state.markFailed(attemptedRevision, error);
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountRoleProvisioningState> findByKeyAndProvider(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider
    ) {
        return jpaRepository.findByAccountIdAndRoleIdAndProvider(
                        accountId.value(),
                        roleId.value(),
                        provider
                )
                .map(this::toDomain);
    }

    private AccountRoleProvisioningJpaEntity requiredEntity(
            AccountId accountId,
            RoleId roleId,
            IdentityProviderType provider
    ) {
        return jpaRepository.findByAccountIdAndRoleIdAndProvider(
                        accountId.value(),
                        roleId.value(),
                        provider
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Account-role provisioning state not found for account "
                                + accountId.value()
                                + ", role "
                                + roleId.value()
                                + " and provider "
                                + provider
                ));
    }

    private AccountRoleProvisioningJpaEntity newEntity(
            AccountRoleProvisioningState state
    ) {
        AccountRoleProvisioningJpaEntity entity = new AccountRoleProvisioningJpaEntity();
        entity.setId(state.getId());
        entity.setAccountId(state.getAccountId().value());
        entity.setRoleId(state.getRoleId().value());
        entity.setProvider(state.getProvider());
        updateEntity(state, entity);
        return entity;
    }

    private void updateEntity(
            AccountRoleProvisioningState state,
            AccountRoleProvisioningJpaEntity entity
    ) {
        entity.setDesiredAssigned(state.isDesiredAssigned());
        entity.setSyncStatus(state.getStatus());
        entity.setDesiredRevision(state.getDesiredRevision());
        entity.setSyncedRevision(state.getSyncedRevision());
        entity.setLastSyncedAt(state.getLastSyncedAt());
        entity.setLastError(state.getLastError());
    }

    private AccountRoleProvisioningState toDomain(
            AccountRoleProvisioningJpaEntity entity
    ) {
        return AccountRoleProvisioningState.rehydrate(
                entity.getId(),
                new AccountId(entity.getAccountId()),
                new RoleId(entity.getRoleId()),
                entity.getProvider(),
                entity.isDesiredAssigned(),
                entity.getSyncStatus(),
                entity.getDesiredRevision(),
                entity.getSyncedRevision(),
                entity.getLastSyncedAt(),
                entity.getLastError()
        );
    }
}