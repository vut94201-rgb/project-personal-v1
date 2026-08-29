package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.access.domain.ServicePrincipalRole;
import com.hanyang.identity.identityservicev4mono.access.domain.ServicePrincipalRoleRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ServicePrincipalRoleRepositoryAdapter
        implements ServicePrincipalRoleRepository {

    private final ServicePrincipalRoleJpaRepository jpaRepository;

    @Override
    public ServicePrincipalRole save(ServicePrincipalRole servicePrincipalRole) {
        ServicePrincipalRoleJpaId id = toJpaId(
                servicePrincipalRole.getServicePrincipalId(),
                servicePrincipalRole.getRoleId()
        );

        ServicePrincipalRoleJpaEntity entity = new ServicePrincipalRoleJpaEntity();
        entity.setId(id);

        ServicePrincipalRoleJpaEntity saved = jpaRepository.save(entity);

        return ServicePrincipalRole.rehydrate(
                new ServicePrincipalId(saved.getId().getServicePrincipalId()),
                new RoleId(saved.getId().getRoleId())
        );
    }

    @Override
    public void delete(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        jpaRepository.deleteById(toJpaId(servicePrincipalId, roleId));
    }

    @Override
    public boolean exists(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        return jpaRepository.existsById(toJpaId(servicePrincipalId, roleId));
    }

    @Override
    public List<RoleId> findRoleIdsByServicePrincipalId(
            ServicePrincipalId servicePrincipalId
    ) {
        return jpaRepository
                .findAllById_ServicePrincipalId(servicePrincipalId.value())
                .stream()
                .map(entity -> new RoleId(entity.getId().getRoleId()))
                .toList();
    }

    private ServicePrincipalRoleJpaId toJpaId(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        return new ServicePrincipalRoleJpaId(
                servicePrincipalId.value(),
                roleId.value()
        );
    }
}