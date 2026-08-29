package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.provisioning;

import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningState;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.ServicePrincipalJpaRepository;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ServicePrincipalProvisioningRepositoryAdapter
    implements ServicePrincipalProvisioningStateRepository {

  private final ServicePrincipalProvisioningJpaRepository jpaRepository;
  private final ServicePrincipalJpaRepository servicePrincipalJpaRepository;

  @Override
  @Transactional
  public ServicePrincipalProvisioningState requestSynchronization(
      ServicePrincipalId servicePrincipalId, IdentityProviderType provider) {
    Optional<ServicePrincipalProvisioningJpaEntity> existing =
        jpaRepository.findByServicePrincipalIdAndProvider(servicePrincipalId.value(), provider);

    if (existing.isEmpty()) {
      ServicePrincipalProvisioningJpaEntity created =
          newEntity(ServicePrincipalProvisioningState.pending(servicePrincipalId, provider));
      return toDomain(jpaRepository.save(created));
    }

    ServicePrincipalProvisioningJpaEntity entity = existing.get();
    ServicePrincipalProvisioningState state = toDomain(entity);
    state.requestSynchronization();
    updateEntity(state, entity);

    return toDomain(jpaRepository.save(entity));
  }

  @Override
  @Transactional
  public ServicePrincipalProvisioningState beginSynchronization(
      ServicePrincipalId servicePrincipalId, IdentityProviderType provider) {
    ServicePrincipalProvisioningJpaEntity entity =
        jpaRepository
            .findByServicePrincipalIdAndProvider(servicePrincipalId.value(), provider)
            .orElseGet(
                () ->
                    newEntity(
                        ServicePrincipalProvisioningState.pending(servicePrincipalId, provider)));

    ServicePrincipalProvisioningState state = toDomain(entity);
    state.beginSynchronization();
    updateEntity(state, entity);

    return toDomain(jpaRepository.save(entity));
  }

  @Override
  @Transactional
  public ServicePrincipalProvisioningState completeSynchronization(
      ServicePrincipalId servicePrincipalId,
      IdentityProviderType provider,
      long synchronizedRevision,
      String externalId,
      String externalCode,
      Instant synchronizedAt) {
    ServicePrincipalProvisioningJpaEntity entity = requiredEntity(servicePrincipalId, provider);
    ServicePrincipalProvisioningState state = toDomain(entity);
    state.markSynchronized(synchronizedRevision, externalId, externalCode, synchronizedAt);
    updateEntity(state, entity);

    return toDomain(jpaRepository.save(entity));
  }

  @Override
  @Transactional
  public ServicePrincipalProvisioningState failSynchronization(
      ServicePrincipalId servicePrincipalId,
      IdentityProviderType provider,
      long attemptedRevision,
      String error) {
    ServicePrincipalProvisioningJpaEntity entity = requiredEntity(servicePrincipalId, provider);
    ServicePrincipalProvisioningState state = toDomain(entity);
    state.markFailed(attemptedRevision, error);
    updateEntity(state, entity);

    return toDomain(jpaRepository.save(entity));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ServicePrincipalProvisioningState> findByServicePrincipalIdAndProvider(
      ServicePrincipalId servicePrincipalId, IdentityProviderType provider) {
    return jpaRepository
        .findByServicePrincipalIdAndProvider(servicePrincipalId.value(), provider)
        .map(this::toDomain);
  }

  private ServicePrincipalProvisioningJpaEntity requiredEntity(
      ServicePrincipalId servicePrincipalId, IdentityProviderType provider) {
    return jpaRepository
        .findByServicePrincipalIdAndProvider(servicePrincipalId.value(), provider)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Service principal provisioning state not found for service principal "
                        + servicePrincipalId.value()
                        + " and provider "
                        + provider));
  }

  private ServicePrincipalProvisioningJpaEntity newEntity(ServicePrincipalProvisioningState state) {
    ServicePrincipalProvisioningJpaEntity entity = new ServicePrincipalProvisioningJpaEntity();
    entity.setId(state.getId());
    entity.setServicePrincipal(
        servicePrincipalJpaRepository.getReferenceById(state.getServicePrincipalId().value()));
    entity.setProvider(state.getProvider());
    updateEntity(state, entity);
    return entity;
  }

  private void updateEntity(
      ServicePrincipalProvisioningState state, ServicePrincipalProvisioningJpaEntity entity) {
    entity.setExternalId(state.getExternalId());
    entity.setExternalCode(state.getExternalCode());
    entity.setSyncStatus(state.getStatus());
    entity.setDesiredRevision(state.getDesiredRevision());
    entity.setSyncedRevision(state.getSyncedRevision());
    entity.setLastSyncedAt(state.getLastSyncedAt());
    entity.setLastError(state.getLastError());
  }

  private ServicePrincipalProvisioningState toDomain(ServicePrincipalProvisioningJpaEntity entity) {
    return ServicePrincipalProvisioningState.rehydrate(
        entity.getId(),
        new ServicePrincipalId(entity.getServicePrincipal().getId()),
        entity.getProvider(),
        entity.getExternalId(),
        entity.getExternalCode(),
        entity.getSyncStatus(),
        entity.getDesiredRevision(),
        entity.getSyncedRevision(),
        entity.getLastSyncedAt(),
        entity.getLastError());
  }
}
