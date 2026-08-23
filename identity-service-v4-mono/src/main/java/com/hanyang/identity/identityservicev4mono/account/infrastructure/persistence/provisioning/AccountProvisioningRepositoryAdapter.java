package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.provisioning;

import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.AccountJpaRepository;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountProvisioningRepositoryAdapter
        implements AccountProvisioningStateRepository {

    private final AccountProvisioningJpaRepository jpaRepository;
    private final AccountJpaRepository accountJpaRepository;

    @Override
    @Transactional
    public AccountProvisioningState requestSynchronization(
            AccountId accountId,
            IdentityProviderType provider
    ) {
        Optional<AccountProvisioningJpaEntity> existing = jpaRepository
                .findByAccountIdAndProvider(accountId.value(), provider);

        if (existing.isEmpty()) {
            return toDomain(jpaRepository.save(
                    newEntity(AccountProvisioningState.pending(accountId, provider))
            ));
        }

        AccountProvisioningJpaEntity entity = existing.get();
        AccountProvisioningState state = toDomain(entity);
        state.requestSynchronization();
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public AccountProvisioningState beginSynchronization(
            AccountId accountId,
            IdentityProviderType provider
    ) {
        AccountProvisioningJpaEntity entity = jpaRepository
                .findByAccountIdAndProvider(accountId.value(), provider)
                .orElseGet(() -> newEntity(
                        AccountProvisioningState.pending(accountId, provider)
                ));

        AccountProvisioningState state = toDomain(entity);
        state.beginSynchronization();
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public AccountProvisioningState completeSynchronization(
            AccountId accountId,
            IdentityProviderType provider,
            long synchronizedRevision,
            String externalId,
            String externalCode,
            Instant synchronizedAt
    ) {
        AccountProvisioningJpaEntity entity = requiredEntity(accountId, provider);
        AccountProvisioningState state = toDomain(entity);
        state.markSynchronized(
                synchronizedRevision,
                externalId,
                externalCode,
                synchronizedAt
        );
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public AccountProvisioningState failSynchronization(
            AccountId accountId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    ) {
        AccountProvisioningJpaEntity entity = requiredEntity(accountId, provider);
        AccountProvisioningState state = toDomain(entity);
        state.markFailed(attemptedRevision, error);
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountProvisioningState> findByAccountIdAndProvider(
            AccountId accountId,
            IdentityProviderType provider
    ) {
        return jpaRepository.findByAccountIdAndProvider(accountId.value(), provider)
                .map(this::toDomain);
    }

    private AccountProvisioningJpaEntity requiredEntity(
            AccountId accountId,
            IdentityProviderType provider
    ) {
        return jpaRepository.findByAccountIdAndProvider(accountId.value(), provider)
                .orElseThrow(() -> new IllegalStateException(
                        "Account provisioning state not found for account "
                                + accountId.value()
                                + " and provider "
                                + provider
                ));
    }

    private AccountProvisioningJpaEntity newEntity(AccountProvisioningState state) {
        AccountProvisioningJpaEntity entity = new AccountProvisioningJpaEntity();
        entity.setId(state.getId());
        entity.setAccount(accountJpaRepository.getReferenceById(state.getAccountId().value()));
        entity.setProvider(state.getProvider());
        updateEntity(state, entity);
        return entity;
    }

    private void updateEntity(
            AccountProvisioningState state,
            AccountProvisioningJpaEntity entity
    ) {
        entity.setExternalId(state.getExternalId());
        entity.setExternalCode(state.getExternalCode());
        entity.setSyncStatus(state.getStatus());
        entity.setDesiredRevision(state.getDesiredRevision());
        entity.setSyncedRevision(state.getSyncedRevision());
        entity.setLastSyncedAt(state.getLastSyncedAt());
        entity.setLastError(state.getLastError());
    }

    private AccountProvisioningState toDomain(AccountProvisioningJpaEntity entity) {
        return AccountProvisioningState.rehydrate(
                entity.getId(),
                new AccountId(entity.getAccount().getId()),
                entity.getProvider(),
                entity.getExternalId(),
                entity.getExternalCode(),
                entity.getSyncStatus(),
                entity.getDesiredRevision(),
                entity.getSyncedRevision(),
                entity.getLastSyncedAt(),
                entity.getLastError()
        );
    }
}