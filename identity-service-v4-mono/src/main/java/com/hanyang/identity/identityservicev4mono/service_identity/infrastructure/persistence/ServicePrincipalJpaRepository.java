package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ServicePrincipalJpaRepository
        extends BaseJpaRepository<ServicePrincipalJpaEntity, UUID> {

    Optional<ServicePrincipalJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

    Set<ServicePrincipalJpaEntity> findAllByStatus(ServicePrincipalStatus status);
}