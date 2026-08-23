package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;

import com.hanyang.identity.identityservicev4mono.access.application.provisioning.RoleProvisioningState;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.RoleProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.RoleJpaRepository;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleProvisioningRepositoryAdapter
        implements RoleProvisioningStateRepository {

    private final RoleProvisioningJpaRepository jpaRepository;
    private final RoleJpaRepository roleJpaRepository;

    @Override
    @Transactional
    public RoleProvisioningState requestSynchronization(
            RoleId roleId,
            IdentityProviderType provider
    ) {
        Optional<RoleProvisioningJpaEntity> existing = jpaRepository
                .findByRoleIdAndProvider(roleId.value(), provider);

        if (existing.isEmpty()) {
            RoleProvisioningJpaEntity created = newEntity(
                    RoleProvisioningState.pending(roleId, provider)
            );
            return toDomain(jpaRepository.save(created));
        }

        RoleProvisioningJpaEntity entity = existing.get();
        RoleProvisioningState state = toDomain(entity);
        state.requestSynchronization();
        updateEntity(state, entity);

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public RoleProvisioningState beginSynchronization(
            RoleId roleId,
            IdentityProviderType provider
    ) {
        RoleProvisioningJpaEntity entity = jpaRepository
                .findByRoleIdAndProvider(roleId.value(), provider)
                .orElseGet(() -> newEntity(
                        RoleProvisioningState.pending(roleId, provider)
                ));

        RoleProvisioningState state = toDomain(entity);
        state.beginSynchronization();
        updateEntity(state, entity);

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public RoleProvisioningState completeSynchronization(
            RoleId roleId,
            IdentityProviderType provider,
            long synchronizedRevision,
            String externalId,
            String externalCode,
            Instant synchronizedAt
    ) {
        RoleProvisioningJpaEntity entity = requiredEntity(roleId, provider);
        RoleProvisioningState state = toDomain(entity);
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
    public RoleProvisioningState failSynchronization(
            RoleId roleId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    ) {
        RoleProvisioningJpaEntity entity = requiredEntity(roleId, provider);
        RoleProvisioningState state = toDomain(entity);
        state.markFailed(attemptedRevision, error);
        updateEntity(state, entity);

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleProvisioningState> findByRoleIdAndProvider(
            RoleId roleId,
            IdentityProviderType provider
    ) {
        return jpaRepository
                .findByRoleIdAndProvider(roleId.value(), provider)
                .map(this::toDomain);
    }

    private RoleProvisioningJpaEntity requiredEntity(
            RoleId roleId,
            IdentityProviderType provider
    ) {
        return jpaRepository
                .findByRoleIdAndProvider(roleId.value(), provider)
                .orElseThrow(() -> new IllegalStateException(
                        "Role provisioning state not found for role "
                                + roleId.value()
                                + " and provider "
                                + provider
                ));
    }

    private RoleProvisioningJpaEntity newEntity(
            RoleProvisioningState state
    ) {
        RoleProvisioningJpaEntity entity = new RoleProvisioningJpaEntity();
        entity.setId(state.getId());
        entity.setRole(
                roleJpaRepository.getReferenceById(state.getRoleId().value())
        );
        entity.setProvider(state.getProvider());
        updateEntity(state, entity);
        return entity;
    }

    private void updateEntity(
            RoleProvisioningState state,
            RoleProvisioningJpaEntity entity
    ) {
        entity.setExternalId(state.getExternalId());
        entity.setExternalCode(state.getExternalCode());
        entity.setSyncStatus(state.getStatus());
        entity.setDesiredRevision(state.getDesiredRevision());
        entity.setSyncedRevision(state.getSyncedRevision());
        entity.setLastSyncedAt(state.getLastSyncedAt());
        entity.setLastError(state.getLastError());
    }

    private RoleProvisioningState toDomain(
            RoleProvisioningJpaEntity entity
    ) {
        return RoleProvisioningState.rehydrate(
                entity.getId(),
                new RoleId(entity.getRole().getId()),
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