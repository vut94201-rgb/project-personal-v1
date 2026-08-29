package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnershipType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicePrincipalOwnerJpaRepository
        extends BaseJpaRepository<ServicePrincipalOwnerJpaEntity, UUID> {

    List<ServicePrincipalOwnerJpaEntity> findAllByServicePrincipalIdAndStatusOrderByCreatedAtAsc(
            UUID servicePrincipalId,
            ServicePrincipalOwnerStatus status
    );

    boolean existsByServicePrincipalIdAndEmployeeIdAndStatus(
            UUID servicePrincipalId,
            UUID employeeId,
            ServicePrincipalOwnerStatus status
    );

    boolean existsByServicePrincipalIdAndOwnershipTypeAndStatus(
            UUID servicePrincipalId,
            ServicePrincipalOwnershipType ownershipType,
            ServicePrincipalOwnerStatus status
    );
}