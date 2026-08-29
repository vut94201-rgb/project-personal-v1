package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicePrincipalRoleJpaRepository
        extends BaseJpaRepository<ServicePrincipalRoleJpaEntity, ServicePrincipalRoleJpaId> {

    List<ServicePrincipalRoleJpaEntity> findAllById_ServicePrincipalId(UUID servicePrincipalId);
}