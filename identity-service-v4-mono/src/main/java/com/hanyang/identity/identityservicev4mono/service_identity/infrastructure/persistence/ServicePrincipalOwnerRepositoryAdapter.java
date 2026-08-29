package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ServicePrincipalOwnerRepositoryAdapter
        implements ServicePrincipalOwnerRepository {

    private final ServicePrincipalOwnerJpaRepository jpaRepository;
    private final ServicePrincipalOwnerPersistenceMapper mapper;

    @Override
    public ServicePrincipalOwner save(ServicePrincipalOwner owner) {
        return jpaRepository.findById(owner.getId().value())
                .map(existing -> {
                    mapper.updateEntity(owner, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> mapper.toDomain(
                        jpaRepository.save(mapper.toEntity(owner))
                ));
    }

    @Override
    public Optional<ServicePrincipalOwner> findById(ServicePrincipalOwnerId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<ServicePrincipalOwner> findAllActiveByServicePrincipalId(
            ServicePrincipalId servicePrincipalId
    ) {
        return jpaRepository
                .findAllByServicePrincipalIdAndStatusOrderByCreatedAtAsc(
                        servicePrincipalId.value(),
                        ServicePrincipalOwnerStatus.ACTIVE
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByServicePrincipalIdAndEmployeeId(
            ServicePrincipalId servicePrincipalId,
            EmployeeId employeeId
    ) {
        return jpaRepository.existsByServicePrincipalIdAndEmployeeIdAndStatus(
                servicePrincipalId.value(),
                employeeId.value(),
                ServicePrincipalOwnerStatus.ACTIVE
        );
    }

    @Override
    public boolean existsActivePrimaryByServicePrincipalId(
            ServicePrincipalId servicePrincipalId
    ) {
        return jpaRepository.existsByServicePrincipalIdAndOwnershipTypeAndStatus(
                servicePrincipalId.value(),
                ServicePrincipalOwnershipType.PRIMARY,
                ServicePrincipalOwnerStatus.ACTIVE
        );
    }
}