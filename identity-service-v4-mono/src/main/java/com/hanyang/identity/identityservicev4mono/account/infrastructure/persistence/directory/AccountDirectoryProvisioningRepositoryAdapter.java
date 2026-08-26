package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.directory;


import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.AccountJpaRepository;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountDirectoryProvisioningRepositoryAdapter
        implements AccountDirectoryProvisioningStateRepository {

    private final AccountDirectoryProvisioningJpaRepository jpaRepository;
    private final AccountJpaRepository accountJpaRepository;

    @Override
    @Transactional
    public AccountDirectoryProvisioningState requestSynchronization(
            AccountId accountId,
            DirectoryProviderType provider
    ) {
        Optional<AccountDirectoryProvisioningJpaEntity> existing =
                jpaRepository.findByAccountIdAndProvider(accountId.value(), provider);

        if (existing.isEmpty()) {
            return toDomain(jpaRepository.save(
                    newEntity(AccountDirectoryProvisioningState.pending(
                            accountId,
                            provider
                    ))
            ));
        }

        AccountDirectoryProvisioningJpaEntity entity = existing.get();
        AccountDirectoryProvisioningState state = toDomain(entity);
        state.requestSynchronization();
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public AccountDirectoryProvisioningState beginSynchronization(
            AccountId accountId,
            DirectoryProviderType provider
    ) {
        AccountDirectoryProvisioningJpaEntity entity = jpaRepository
                .findByAccountIdAndProvider(accountId.value(), provider)
                .orElseGet(() -> newEntity(
                        AccountDirectoryProvisioningState.pending(accountId, provider)
                ));

        AccountDirectoryProvisioningState state = toDomain(entity);
        state.beginSynchronization();
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public AccountDirectoryProvisioningState completeSynchronization(
            AccountId accountId,
            DirectoryProviderType provider,
            long synchronizedRevision,
            String externalDn,
            String externalCode,
            Instant synchronizedAt
    ) {
        AccountDirectoryProvisioningJpaEntity entity = requiredEntity(
                accountId,
                provider
        );
        AccountDirectoryProvisioningState state = toDomain(entity);
        state.markSynchronized(
                synchronizedRevision,
                externalDn,
                externalCode,
                synchronizedAt
        );
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public AccountDirectoryProvisioningState failSynchronization(
            AccountId accountId,
            DirectoryProviderType provider,
            long attemptedRevision,
            String error
    ) {
        AccountDirectoryProvisioningJpaEntity entity = requiredEntity(
                accountId,
                provider
        );
        AccountDirectoryProvisioningState state = toDomain(entity);
        state.markFailed(attemptedRevision, error);
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountDirectoryProvisioningState> findByAccountIdAndProvider(
            AccountId accountId,
            DirectoryProviderType provider
    ) {
        return jpaRepository.findByAccountIdAndProvider(accountId.value(), provider)
                .map(this::toDomain);
    }

    private AccountDirectoryProvisioningJpaEntity requiredEntity(
            AccountId accountId,
            DirectoryProviderType provider
    ) {
        return jpaRepository.findByAccountIdAndProvider(accountId.value(), provider)
                .orElseThrow(() -> new IllegalStateException(
                        "Account directory provisioning state not found for account "
                                + accountId.value()
                                + " and provider "
                                + provider
                ));
    }

    private AccountDirectoryProvisioningJpaEntity newEntity(
            AccountDirectoryProvisioningState state
    ) {
        AccountDirectoryProvisioningJpaEntity entity =
                new AccountDirectoryProvisioningJpaEntity();
        entity.setId(state.getId());
        entity.setAccount(
                accountJpaRepository.getReferenceById(state.getAccountId().value())
        );
        entity.setProvider(state.getProvider());
        updateEntity(state, entity);
        return entity;
    }

    private void updateEntity(
            AccountDirectoryProvisioningState state,
            AccountDirectoryProvisioningJpaEntity entity
    ) {
        entity.setExternalDn(state.getExternalDn());
        entity.setExternalCode(state.getExternalCode());
        entity.setSyncStatus(state.getStatus());
        entity.setDesiredRevision(state.getDesiredRevision());
        entity.setSyncedRevision(state.getSyncedRevision());
        entity.setLastSyncedAt(state.getLastSyncedAt());
        entity.setLastError(state.getLastError());
    }

    private AccountDirectoryProvisioningState toDomain(
            AccountDirectoryProvisioningJpaEntity entity
    ) {
        return AccountDirectoryProvisioningState.rehydrate(
                entity.getId(),
                new AccountId(entity.getAccount().getId()),
                entity.getProvider(),
                entity.getExternalDn(),
                entity.getExternalCode(),
                entity.getSyncStatus(),
                entity.getDesiredRevision(),
                entity.getSyncedRevision(),
                entity.getLastSyncedAt(),
                entity.getLastError()
        );
    }
}