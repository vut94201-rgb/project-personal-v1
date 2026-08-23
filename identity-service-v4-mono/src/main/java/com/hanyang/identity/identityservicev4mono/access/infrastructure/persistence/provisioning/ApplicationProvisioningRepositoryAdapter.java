package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;

import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ApplicationProvisioningState;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ApplicationProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.ApplicationJpaRepository;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ApplicationProvisioningRepositoryAdapter
        implements ApplicationProvisioningStateRepository {

    private final ApplicationProvisioningJpaRepository jpaRepository;
    private final ApplicationJpaRepository applicationJpaRepository;

    @Override
    @Transactional
    public ApplicationProvisioningState requestSynchronization(
            ApplicationId applicationId,
            IdentityProviderType provider
    ) {
        Optional<ApplicationProvisioningJpaEntity> existing = jpaRepository
                .findByApplicationIdAndProvider(applicationId.value(), provider);

        if (existing.isEmpty()) {
            ApplicationProvisioningJpaEntity created = newEntity(
                    ApplicationProvisioningState.pending(applicationId, provider)
            );
            return toDomain(jpaRepository.save(created));
        }

        ApplicationProvisioningJpaEntity entity = existing.get();
        ApplicationProvisioningState state = toDomain(entity);
        state.requestSynchronization();
        updateEntity(state, entity);

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public ApplicationProvisioningState beginSynchronization(
            ApplicationId applicationId,
            IdentityProviderType provider
    ) {
        ApplicationProvisioningJpaEntity entity = jpaRepository
                .findByApplicationIdAndProvider(applicationId.value(), provider)
                .orElseGet(() -> newEntity(
                        ApplicationProvisioningState.pending(applicationId, provider)
                ));

        ApplicationProvisioningState state = toDomain(entity);
        state.beginSynchronization();
        updateEntity(state, entity);

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public ApplicationProvisioningState completeSynchronization(
            ApplicationId applicationId,
            IdentityProviderType provider,
            long synchronizedRevision,
            String externalId,
            String externalCode,
            Instant synchronizedAt
    ) {
        ApplicationProvisioningJpaEntity entity = requiredEntity(applicationId, provider);
        ApplicationProvisioningState state = toDomain(entity);
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
    public ApplicationProvisioningState failSynchronization(
            ApplicationId applicationId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    ) {
        ApplicationProvisioningJpaEntity entity = requiredEntity(applicationId, provider);
        ApplicationProvisioningState state = toDomain(entity);
        state.markFailed(attemptedRevision, error);
        updateEntity(state, entity);

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationProvisioningState> findByApplicationIdAndProvider(
            ApplicationId applicationId,
            IdentityProviderType provider
    ) {
        return jpaRepository
                .findByApplicationIdAndProvider(applicationId.value(), provider)
                .map(this::toDomain);
    }

    private ApplicationProvisioningJpaEntity requiredEntity(
            ApplicationId applicationId,
            IdentityProviderType provider
    ) {
        return jpaRepository
                .findByApplicationIdAndProvider(applicationId.value(), provider)
                .orElseThrow(() -> new IllegalStateException(
                        "Application provisioning state not found for application "
                                + applicationId.value()
                                + " and provider "
                                + provider
                ));
    }

    private ApplicationProvisioningJpaEntity newEntity(
            ApplicationProvisioningState state
    ) {
        ApplicationProvisioningJpaEntity entity = new ApplicationProvisioningJpaEntity();
        entity.setId(state.getId());
        entity.setApplication(
                applicationJpaRepository.getReferenceById(state.getApplicationId().value())
        );
        entity.setProvider(state.getProvider());
        updateEntity(state, entity);
        return entity;
    }

    private void updateEntity(
            ApplicationProvisioningState state,
            ApplicationProvisioningJpaEntity entity
    ) {
        entity.setExternalId(state.getExternalId());
        entity.setExternalCode(state.getExternalCode());
        entity.setSyncStatus(state.getStatus());
        entity.setDesiredRevision(state.getDesiredRevision());
        entity.setSyncedRevision(state.getSyncedRevision());
        entity.setLastSyncedAt(state.getLastSyncedAt());
        entity.setLastError(state.getLastError());
    }

    private ApplicationProvisioningState toDomain(
            ApplicationProvisioningJpaEntity entity
    ) {
        return ApplicationProvisioningState.rehydrate(
                entity.getId(),
                new ApplicationId(entity.getApplication().getId()),
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