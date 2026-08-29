package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningState;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ServicePrincipalRoleProvisioningRepositoryAdapter
        implements ServicePrincipalRoleProvisioningStateRepository {

    private final ServicePrincipalRoleProvisioningJpaRepository jpaRepository;

    @Override
    @Transactional
    public ServicePrincipalRoleProvisioningState requestSynchronization(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            boolean desiredAssigned
    ) {
        Optional<ServicePrincipalRoleProvisioningJpaEntity> existing =
                jpaRepository.findByServicePrincipalIdAndRoleIdAndProvider(
                        servicePrincipalId.value(),
                        roleId.value(),
                        provider
                );

        if (existing.isEmpty()) {
            return toDomain(jpaRepository.save(
                    newEntity(ServicePrincipalRoleProvisioningState.pending(
                            servicePrincipalId,
                            roleId,
                            provider,
                            desiredAssigned
                    ))
            ));
        }

        ServicePrincipalRoleProvisioningJpaEntity entity = existing.get();
        ServicePrincipalRoleProvisioningState state = toDomain(entity);
        state.requestSynchronization(desiredAssigned);
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public ServicePrincipalRoleProvisioningState beginSynchronization(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider
    ) {
        ServicePrincipalRoleProvisioningJpaEntity entity = requiredEntity(
                servicePrincipalId,
                roleId,
                provider
        );
        ServicePrincipalRoleProvisioningState state = toDomain(entity);
        state.beginSynchronization();
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public ServicePrincipalRoleProvisioningState completeSynchronization(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            long synchronizedRevision,
            Instant synchronizedAt
    ) {
        ServicePrincipalRoleProvisioningJpaEntity entity = requiredEntity(
                servicePrincipalId,
                roleId,
                provider
        );
        ServicePrincipalRoleProvisioningState state = toDomain(entity);
        state.markSynchronized(synchronizedRevision, synchronizedAt);
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public ServicePrincipalRoleProvisioningState failSynchronization(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider,
            long attemptedRevision,
            String error
    ) {
        ServicePrincipalRoleProvisioningJpaEntity entity = requiredEntity(
                servicePrincipalId,
                roleId,
                provider
        );
        ServicePrincipalRoleProvisioningState state = toDomain(entity);
        state.markFailed(attemptedRevision, error);
        updateEntity(state, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServicePrincipalRoleProvisioningState> findByKeyAndProvider(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider
    ) {
        return jpaRepository.findByServicePrincipalIdAndRoleIdAndProvider(
                        servicePrincipalId.value(),
                        roleId.value(),
                        provider
                )
                .map(this::toDomain);
    }

    private ServicePrincipalRoleProvisioningJpaEntity requiredEntity(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            IdentityProviderType provider
    ) {
        return jpaRepository.findByServicePrincipalIdAndRoleIdAndProvider(
                        servicePrincipalId.value(),
                        roleId.value(),
                        provider
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Service-principal-role provisioning state not found for servicePrincipal "
                                + servicePrincipalId.value()
                                + ", role "
                                + roleId.value()
                                + " and provider "
                                + provider
                ));
    }

    private ServicePrincipalRoleProvisioningJpaEntity newEntity(
            ServicePrincipalRoleProvisioningState state
    ) {
        ServicePrincipalRoleProvisioningJpaEntity entity =
                new ServicePrincipalRoleProvisioningJpaEntity();
        entity.setId(state.getId());
        entity.setServicePrincipalId(
                state.getServicePrincipalId().value()
        );
        entity.setRoleId(state.getRoleId().value());
        entity.setProvider(state.getProvider());
        updateEntity(state, entity);
        return entity;
    }

    private void updateEntity(
            ServicePrincipalRoleProvisioningState state,
            ServicePrincipalRoleProvisioningJpaEntity entity
    ) {
        entity.setDesiredAssigned(state.isDesiredAssigned());
        entity.setSyncStatus(state.getStatus());
        entity.setDesiredRevision(state.getDesiredRevision());
        entity.setSyncedRevision(state.getSyncedRevision());
        entity.setLastSyncedAt(state.getLastSyncedAt());
        entity.setLastError(state.getLastError());
    }

    private ServicePrincipalRoleProvisioningState toDomain(
            ServicePrincipalRoleProvisioningJpaEntity entity
    ) {
        return ServicePrincipalRoleProvisioningState.rehydrate(
                entity.getId(),
                new ServicePrincipalId(entity.getServicePrincipalId()),
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